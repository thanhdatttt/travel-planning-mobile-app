import bcrypt from "bcrypt";
import { Request, Response } from "express";
import { prisma } from "../libs/prisma";
import { createResponse } from "../utils/response";
import { genAccessToken, genRefreshToken, ACCESS_TOKEN_TTL, REFRESH_TOKEN_TTL } from "../utils/jwt";
import { EMAIL_TOKEN_TTL, generateOTP } from "../utils/otp";
import { sendOTPEmail } from "../utils/email";

export const signUp = async (req: Request, res: Response) => {
    try {
        const { email, username, password } = req.body;
        if (!email || !username || !password) {
            return res.status(400)
            .json(createResponse({message: "Bad request", error: "Missing fields"}));
        }

        // check existing email
        const existingEmail = await prisma.user.findUnique({
            where: { email }
        });
        if (existingEmail) {
        return res.status(400).json(
            createResponse({ message: "Email already registered" })
        );
        }

        // Check verify email
        const otpRecord = await prisma.emailOTP.findFirst({
            where: {
                email,
                verified: true
            },
            orderBy: { createdAt: "desc" }
        });
        if (!otpRecord) {
            return res.status(403)
                .json(createResponse({ message: "Email not verified" }));
        }

        // check existing username
        const existingUsername = await prisma.user.findUnique({
            where: {
                username,
            }
        });
        if (existingUsername) {
            return res.status(400)
            .json(createResponse({message: "Bad request", error: "Username already used"}));
        }

        // create new user
        const user = await prisma.user.create({
            data: {
                email,
                username,
                authProviders: {
                    create: {
                        provider: "local",
                        hashPassword: await bcrypt.hash(password, 10),
                    }
                }
            }
        });
        // delete OTP after creating user
        await prisma.emailOTP.deleteMany({
            where: { email }
        });

        return res.status(201).json(createResponse({message: "Sign up successfully", data: user}));
    } catch (err: any) {
        console.log("Error when signing up: ", err.message);
        return res.status(500).json(createResponse({message: "System error", error: err.message}));
    }
}

export const signIn = async (req: Request, res: Response) => {
    try {
        const { usernameOrEmail, password } = req.body;
        if (!usernameOrEmail || !password) {
            return res.status(400)
            .json(createResponse({message: "Bad request", error: "Missing field"}));
        }

        // check existing user
        const user = await prisma.user.findFirst({
            where: {
                OR: [
                    {email: usernameOrEmail},
                    {username: usernameOrEmail},
                ]
            },
            include: {
                authProviders: true,
            }
        });
        if (!user) {
            return res.status(400)
            .json(createResponse({message: "Bad request", error: "Username or email is incorrect"}));
        }

        // check local provider
        const localAuth = user.authProviders.find(p => p.provider === "local");
        if (!localAuth || !localAuth.hashPassword) {
            return res.status(401)
            .json(createResponse({message: "Unauthorized", error: "Account uses social login"}));
        }

        // check password
        const isMatch = await bcrypt.compare(password, localAuth.hashPassword);
        if (!isMatch) {
            return res.status(400)
            .json(createResponse({message: "Bad request", error: "Password is incorrect"}));
        }

        // generate tokens
        const accessToken = genAccessToken(user.id);
        const refreshToken = genRefreshToken(user.id);
        await prisma.session.create({
            data: {
                userId: user.id,
                token: refreshToken,
                expiresAt: new Date(Date.now() + REFRESH_TOKEN_TTL),
            }
        });

        // set cookie
        res.cookie("session", refreshToken, {
            httpOnly: true,
            secure: true,
            sameSite: "none",
            path: "/",
            maxAge: REFRESH_TOKEN_TTL,
        });
        return res.status(200)
        .json(createResponse({message: "Sign in successfully", data: {accessToken, refreshToken}}));
    } catch (err: any) {
        console.log("Error when signing in: ", err.message);
        return res.status(500).json(createResponse({message: "System error", error: err.message}));
    }
}

export const signOut = async (req: Request,res: Response) =>{
    try {
        const token = req.cookies.session;
        if (!token) {
            return res.status(401)
            .json(createResponse({message: "Unathorized", error: "Token is missing"}));
        }

        // clear cookie and session
        const session = await prisma.session.findFirst({
            where: {
                token,
                expiresAt: { gt: new Date() }
            },
            include: {
                user: true
            }
        });
        if (session) {
            await prisma.session.delete({
                where: {
                    id: session.id,
                }
            });
        }
        res.clearCookie("session");

        return res.status(204).json(createResponse({message: "Sign out successfully"}));
    } catch (err: any) {
        console.log("Error when signing out: ", err.message);
        return res.status(500).json(createResponse({message: "System error", error: err.message}));
    }
}

export const verifyOTP = async (req: Request, res: Response) => {
    try {
        const { email, otp } = req.body;
        if (!email || !otp) {
            return res.status(400)
                .json(createResponse({ message: "Missing fields" }));
        }

        // check otp exist or expired
        const record = await prisma.emailOTP.findFirst({
            where: {email},
            orderBy: {createdAt: "desc"}
        });
        if (!record) {
            return res.status(404)
            .json(createResponse({message: "Not found", error: "Invalid OTP or email"}));
        }
        if (record.expiresAt < new Date(Date.now())) {
            return res.status(403)
            .json(createResponse({message: "Forbidden", error: "OTP is expired"}));
        }

        // verify
        const isMatch = await bcrypt.compare(otp, record.otp);
        if (!isMatch) {
            return res.status(400)
                .json(createResponse({ message: "Invalid OTP" }));
        }
        await prisma.emailOTP.update({
            where: {id: record.id},
            data: { verified: true},
        });

        return res.status(200).json(createResponse({message: "Email verified successfully"}));
    } catch (err: any) {
        console.log("Error when verifying otp: ", err.message);
        return res.status(500).json(createResponse({message: "System error", error: err.message}));
    }
}

export const sendOTP = async (req: Request, res: Response) => {
    try {
        const { email } = req.body;
        if (!email) {
            return res.status(400)
                .json(createResponse({ message: "Missing email" }));
        }

        // Check existing email
        const existingUser = await prisma.user.findUnique({
            where: { email }
        });
        if (existingUser) {
            return res.status(400)
                .json(createResponse({ message: "Email already used" }));
        }

        // delete old OTP and send new OTP
        await prisma.emailOTP.deleteMany({ where: { email } });
        const otp = generateOTP();
        const hashedOTP = await bcrypt.hash(otp, 5);
        await prisma.emailOTP.create({
            data: {
                email,
                otp: hashedOTP,
                expiresAt: new Date(Date.now() + EMAIL_TOKEN_TTL),
            }
        });
        await sendOTPEmail(email, otp);

        return res.status(201).json(createResponse({message: "OTP sent to email"}));
    } catch (err: any) {
        console.log("Error when sending otp: ", err.message);
        return res.status(500).json(createResponse({message: "System error", error: err.message}));
    }
}