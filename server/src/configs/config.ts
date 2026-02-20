import dotenv from "dotenv";

dotenv.config();

export const config = {
    PORT: process.env.PORT,
    DATABASE_URL: process.env.DATABASE_URL,

    // JWT
    JWT_SECRET_KEY: process.env.JWT_SECRET_KEY,
    JWT_REFRESH_SECRET_KEY: process.env.JWT_REFRESH_SECRET_KEY,

    // RESEND
    RESEND_API_KEY: process.env.RESEND_API_KEY,
    RESEND_SEND_EMAIL: process.env.RESEND_SEND_EMAIL,
}