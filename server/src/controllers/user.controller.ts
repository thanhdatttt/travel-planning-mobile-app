import { Request, Response } from "express";
import { createResponse } from "../utils/response";

export const getMe = async (req: Request & {user?: any}, res: Response) => {
    try {
        return res.status(200).json(createResponse({message: "Get user successfully", data: req.user}));
    } catch (err: any) {
        console.log("Error when getting user info: ", err.message);
        return res.status(500).json(createResponse({message: "System error", error: err.message}));
    }
}