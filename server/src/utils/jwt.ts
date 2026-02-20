import jwt from "jsonwebtoken";
import { config } from "../configs/config";

export const ACCESS_TOKEN_TTL = "15m";
export const REFRESH_TOKEN_TTL = 7 * 24 * 60 * 60 * 1000;

export const genAccessToken = (userId: string) => {
    return jwt.sign(userId, config.JWT_SECRET_KEY as string, {
        expiresIn: ACCESS_TOKEN_TTL,
    });
}

export const genRefreshToken = (userId: string) => {
    return jwt.sign(userId, config.JWT_REFRESH_SECRET_KEY as string, {
        expiresIn: REFRESH_TOKEN_TTL,
    });
}