import jwt from "jsonwebtoken";
import { config } from "../configs/config";
import { prisma } from "../libs/prisma";
import { createResponse } from "../utils/response";
import { NextFunction, Request, Response } from "express";

export const requireAuth = async (req: Request & {user?: any}, res: Response, next: NextFunction) => {
    try {
        // get token
        const header = req.headers.authorization;
        const token = header && header.split(" ")[1];
        if (!token) {
            return res.status(400)
            .json(createResponse({message: "Unauthorized", error: "No token provided"}));
        }

        try {
            const decoded = jwt.verify(token, config.JWT_SECRET_KEY as string) as { userId: string; role: string };
            const user = await prisma.user.findUnique({
                where: { id: decoded.userId },
            });
            if (!user) {
                return res.status(404)
                .json(createResponse({message: "Not found", error: "User not found"}));
            }
    
            req.user = user;
            next();
        } catch (error: any) {
            return res.status(401)
            .json(createResponse({message: "Unathorized", error: "Invalid or expired token"}));
        }
    } catch (err: any) {
        console.log("Error when authorizing user: ", err.message);
        return res.status(500).json(createResponse({message: "System error", error: err.message}));
    }
}

export const requireRole = (role: "admin" | "user" | "moderator") => {
    return (req: Request & {user?: any}, res: Response, next: NextFunction) => {
        if (!req.user) {
            return res.status(401)
            .json(createResponse({message: "Unauthorized", error: "Unauthenticated"}));
        }

        if (req.user.role !== role) {
            return res.status(403).json(createResponse({message: "Forbidden", error: "You are not allowed"}));
        }

        next();
    }
}