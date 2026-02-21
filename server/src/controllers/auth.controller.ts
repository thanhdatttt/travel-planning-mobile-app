import bcrypt from "bcrypt";
import { Request, Response } from "express";
import { prisma } from "../libs/prisma";
import { config } from "../configs/config";
import { OAuth2Client } from "google-auth-library";
import { createResponse } from "../utils/response";
import { genAccessToken, genRefreshToken, REFRESH_TOKEN_TTL } from "../utils/jwt";
import { EMAIL_TOKEN_TTL, generateOTP } from "../utils/otp";
import { sendOTPResetPass, sendOTPVerification } from "../utils/email";
import axios from "axios";

export const signUp = async (req: Request, res: Response) => {
    try {
        const { email, username, password } = req.body;

        // check existing email
        const existingEmail = await prisma.user.findUnique({
            where: { email }
        });
        if (existingEmail) {
        return res.status(400)
            .json(createResponse({ message: "Email already registered" }));
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

        // Check verify email
        const otpRecord = await prisma.emailOTP.findFirst({
            where: {
                email,
                verified: true,
                expiresAt: { gt: new Date(Date.now()) },
            },
            orderBy: { createdAt: "desc" }
        });
        if (!otpRecord) {
            return res.status(403)
                .json(createResponse({ message: "Email not verified" }));
        }

        // create new user
        const user = await prisma.user.create({
            data: {
                email,
                username,
                authProviders: {
                    create: {
                        provider: "local",
                        providerId: email,
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
            .json(createResponse({message: "Bad request", error: "Username(email) or password is incorrect"}));
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
            .json(createResponse({message: "Bad request", error: "Username(email) or password is incorrect"}));
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
        return res.status(200)
        .json(createResponse({message: "Sign in successfully", data: {accessToken, refreshToken}}));
    } catch (err: any) {
        console.log("Error when signing in: ", err.message);
        return res.status(500).json(createResponse({message: "System error", error: err.message}));
    }
}

export const signOut = async (req: Request,res: Response) =>{
    try {
        const {refreshToken} = req.body;
        if (!refreshToken) {
            return res.status(401)
            .json(createResponse({message: "Unathorized", error: "Token is missing"}));
        }

        // clear session
        const session = await prisma.session.findFirst({
            where: {
                token: refreshToken,
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

        return res.status(204).send();
    } catch (err: any) {
        console.log("Error when signing out: ", err.message);
        return res.status(500).json(createResponse({message: "System error", error: err.message}));
    }
}

export const verifyOTP = async (req: Request, res: Response) => {
    try {
        const { email, otp, type } = req.body;
        if (!email || !otp || (type !== "register" && type !== "reset")) {
            return res.status(400)
                .json(createResponse({ message: "Missing or invalid fields" }));
        }

        // check otp exist or expired
        const record = await prisma.emailOTP.findFirst({
            where: {email, verified: false, type: type},
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
            where: {id: record.id, type: type},
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
        const { email, type } = req.body;
        if (!email || (type !== "register" && type !== "reset")) {
            return res.status(400)
                .json(createResponse({ message: "Bad request", error: "Missing or invalid fields" }));
        }

        // register otp
        if (type === "register") {
            // check existing email
            const existingUser = await prisma.user.findUnique({
                where: { email }
            });
            if (existingUser) {
                return res.status(400)
                    .json(createResponse({ message: "Bad request", error: "Email already used" }));
            }
    
            // delete old OTP
            const latestOTP = await prisma.emailOTP.findFirst({
                where: { email, type: "register" },
                orderBy: { createdAt: "desc" }
            });
            if (latestOTP && Date.now() - latestOTP.createdAt.getTime() < 60000) {
                return res.status(429)
                .json(createResponse({ message: "Not out of time", error: "Please wait before requesting another OTP" }));
            }
            await prisma.emailOTP.deleteMany({ where: { email, type: "register" } });
    
            // send otp
            const otp = generateOTP();
            const hashedOTP = await bcrypt.hash(otp, 6);
            await prisma.emailOTP.create({
                data: {
                    email,
                    otp: hashedOTP,
                    expiresAt: new Date(Date.now() + EMAIL_TOKEN_TTL),
                }
            });
            await sendOTPVerification(email, otp);
        }
        // reset pass otp 
        else {
            // check existing email
            const existingUser = await prisma.user.findUnique({
                where: { email }
            });
            if (!existingUser) {
                return res.status(404)
                    .json(createResponse({ message: "Not found", error: "Email not found" }));
            }

            // delete old OTP
            const latestOTP = await prisma.emailOTP.findFirst({
                where: { email, type: "reset" },
                orderBy: { createdAt: "desc" }
            });
            if (latestOTP && Date.now() - latestOTP.createdAt.getTime() < 60000) {
                return res.status(429)
                .json(createResponse({ message: "Not out of time", error: "Please wait before requesting another OTP" }));
            }
            await prisma.emailOTP.deleteMany({ where: { email, type: "reset" } });

            // send otp
            const otp = generateOTP();
            const hashedOTP = await bcrypt.hash(otp, 6);
            await prisma.emailOTP.create({
                data: {
                    email,
                    otp: hashedOTP,
                    type: "reset",
                    expiresAt: new Date(Date.now() + EMAIL_TOKEN_TTL),
                }
            });
            await sendOTPResetPass(email, otp);
        }

        return res.status(201).json(createResponse({message: "OTP sent to email"}));
    } catch (err: any) {
        console.log("Error when sending otp: ", err.message);
        return res.status(500).json(createResponse({message: "System error", error: err.message}));
    }
}

// oauth
const ggClient = new OAuth2Client({
    clientId: config.GG_CLIENT_ID as string,
});
export const googleAuth = async (req: Request, res: Response) => {
    try {
        const {idToken} = req.body;

        // verify token
        const ticket = await ggClient.verifyIdToken({
            idToken,
            audience: config.GG_CLIENT_ID as string,
        });

        const payload = ticket.getPayload();
        if (!payload?.email) {
            return res.status(401)
            .json(createResponse({message: "Unauthorized", error: "Invalid token"}));
        }
        
        // check exist provider
        const existingProvider = await prisma.authProvider.findUnique({
            where: {
                provider_providerId: {
                    provider: "google",
                    providerId: payload.sub // google id
                }
            },
            include: { user: true },
        });
        let user;
        // get user when exist provider
        if (existingProvider) {
            user = existingProvider.user;
        } else {
            user = await prisma.user.findUnique({
                where: {email: payload.email}
            });
            // create new user if user not exist
            if (!user) {
                user = await prisma.user.create({
                    data: {
                        email: payload.email,
                        avatarUrl: payload.picture ?? null,
                        username: `gg_user_${payload.given_name}_${Date.now()}`,
                        fullName: `${payload.given_name} ${payload.family_name}`,
                        authProviders: {
                            create: {
                                provider: "google",
                                providerId: payload.sub,
                            }
                        }
                    }
                })
            } else {
                // add new provider if user exist
                await prisma.authProvider.create({
                    data: {
                        provider: "google",
                        providerId: payload.sub,
                        userId: user.id,
                    }
                });
            }
        }

        // gen token
        const accessToken = genAccessToken(user.id);
        const refreshToken = genRefreshToken(user.id);
        await prisma.session.create({
            data: {
                userId: user.id,
                token: refreshToken,
                expiresAt: new Date(Date.now() + REFRESH_TOKEN_TTL),
            }
        });

        return res.status(200)
        .json(createResponse({message: "Google auth successfully", data: {accessToken, refreshToken, user}}));
    } catch (err: any) {
        console.log("Error when google auth: ", err.message);
        return res.status(500).json(createResponse({message: "System error", error: err.message}));
    }
}

export const facebookAuth = async (req: Request, res: Response) => {
    try {
        const {accessToken} = req.body;
        if (!accessToken) {
            return res.status(400).json(createResponse({message: "Bad request", error: "Missing token"}));
        }

        const response = await axios.get("https://graph.facebook.com/me", {
            params: {
                fields: "id,name,email,picture",
                access_token: accessToken,
            }
        });

        const { id: facebookId, email, name, picture } = response.data;
        if (!email) {
            return res.status(400)
            .json(createResponse({message: "Bad request", error: "Facebook account has no email"}));
        }
        const existingProvider = await prisma.authProvider.findUnique({
            where: {
                provider_providerId: {
                    provider: "facebook",
                    providerId: facebookId
                }
            },
            include: { user: true }
        });
        let user;
        if (existingProvider) {
            // get user when exist provider
            user = existingProvider.user;
        } else {
            user = await prisma.user.findUnique({
                where: { email }
            });
            if (!user) {
                // create new user if user not exist
                user = await prisma.user.create({
                    data: {
                        email,
                        username: `fb_user_${Date.now()}`,
                        fullName: name,
                        avatarUrl: picture?.data?.url,
                        authProviders: {
                            create: {
                                provider: "facebook",
                                providerId: facebookId
                            }
                        }
                    }
                });
            } else {
                // add new provider if user exist
                await prisma.authProvider.create({
                    data: {
                        provider: "facebook",
                        providerId: facebookId,
                        userId: user.id
                    }
                });
            }
        }

        // gen token
        const accessTokenJWT = genAccessToken(user.id);
        const refreshToken = genRefreshToken(user.id);
        await prisma.session.create({
            data: {
                userId: user.id,
                token: refreshToken,
                expiresAt: new Date(Date.now() + REFRESH_TOKEN_TTL),
            }
        });

        return res.status(200)
        .json(createResponse({message: "Facebook auth successfully", data: {accessTokenJWT, refreshToken, user}}));
    } catch (err: any) {
        console.log("Error when facebook auth: ", err.message);
        return res.status(500).json(createResponse({message: "System error", error: err.message}));
    }
}