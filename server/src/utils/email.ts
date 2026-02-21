import { Resend } from "resend";
import { config } from "../configs/config";

const resend = new Resend(config.RESEND_API_KEY as string);

export const sendOTPVerification = async (email: string, otp: string) => {
    try {
        await resend.emails.send({
            from: config.RESEND_SEND_EMAIL as string,
            to: [email],
            subject: "Email OTP verification",
            html: `
                <div style="
                    max-width: 600px;
                    margin: 0 auto;
                    background-color: #ffffff;
                    border-radius: 8px;
                    padding: 32px;
                    font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
                    color: #1f2937;
                    box-shadow: 0 8px 24px rgba(0,0,0,0.08);
                ">
                    <h2 style="
                        margin: 0 0 16px 0;
                        font-size: 24px;
                        font-weight: 700;
                        letter-spacing: 0.5px;
                        text-align: center;
                    ">
                        WELCOME TO TOURGUILD
                    </h2>

                    <p style="
                        font-size: 16px;
                        line-height: 1.6;
                        margin: 0 0 24px 0;
                        text-align: center;
                        color: #374151;
                    ">
                        Thank you for registering!  
                        Please verify your email address by entering this otp.
                    </p>

                    <div style="text-align: center; margin: 32px 0; font-size: 24px;">
                        ${otp}
                    </div>

                    <p style="
                        font-size: 14px;
                        line-height: 1.6;
                        color: #6b7280;
                        text-align: center;
                        margin: 0 0 24px 0;
                    ">
                        This otp will expire in <strong>5 minutes</strong>.
                        If you did not request this, you can safely ignore this email.
                    </p>

                    <hr style="
                        border: none;
                        border-top: 1px solid #e5e7eb;
                        margin: 24px 0;
                    " />

                    <p style="
                        font-size: 12px;
                        color: #9ca3af;
                        text-align: center;
                        margin: 0;
                    ">
                        © 2026 TourGuild. All rights reserved.
                    </p>
                </div>
            `,
        });
    } catch (err: any) {
        console.log("Error when sending otp email: ", err.message);
        throw err;
    }
}

export const sendOTPResetPass = async (email: string, otp: string) => {
    try {
        await resend.emails.send({
            from: config.RESEND_SEND_EMAIL as string,
            to: [email],
            subject: "Reset password OTP",
            html: `
                <div style="
                    max-width: 600px;
                    margin: 0 auto;
                    background-color: #ffffff;
                    border-radius: 8px;
                    padding: 32px;
                    font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
                    color: #1f2937;
                    box-shadow: 0 8px 24px rgba(0,0,0,0.08);
                ">
                    <h2 style="
                        margin: 0 0 16px 0;
                        font-size: 24px;
                        font-weight: 700;
                        letter-spacing: 0.5px;
                        text-align: center;
                    ">
                        RESET ACCOUNT PASSWORD
                    </h2>

                    <p style="
                        font-size: 16px;
                        line-height: 1.6;
                        margin: 0 0 24px 0;
                        text-align: center;
                        color: #374151;
                    ">  
                        Please verify your email address by entering the below otp before reseting your password account.
                    </p>

                    <div style="text-align: center; margin: 32px 0; font-size: 24px;">
                        ${otp}
                    </div>

                    <p style="
                        font-size: 14px;
                        line-height: 1.6;
                        color: #6b7280;
                        text-align: center;
                        margin: 0 0 24px 0;
                    ">
                        This otp will expire in <strong>5 minutes</strong>.
                        If you did not request this, you can safely ignore this email.
                    </p>

                    <hr style="
                        border: none;
                        border-top: 1px solid #e5e7eb;
                        margin: 24px 0;
                    " />

                    <p style="
                        font-size: 12px;
                        color: #9ca3af;
                        text-align: center;
                        margin: 0;
                    ">
                        © 2026 TourGuild. All rights reserved.
                    </p>
                </div>
            `,
        });
    } catch (err: any) {
        console.log("Error when sending otp email: ", err.message);
        throw err;
    }
}