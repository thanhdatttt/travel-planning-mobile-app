import { Request, Response } from "express";
import { prisma } from "../../libs/prisma";
import { createResponse } from "../../utils/response";

export const getSignInLog = async (req: Request, res: Response) => {
    const now = new Date();
    const startDate = new Date(now.getFullYear(), now.getMonth(), 1);
    const startOfNextMonth = new Date(now.getFullYear(), now.getMonth() + 1, 1);
    const result = await prisma.session.findMany({
        where: {
            AND: [
                {createdAt: {
                    gte: startDate
                }},
                {createdAt: {lte: startOfNextMonth}}
            ]
        }
    });

    return res.status(200).json(
        createResponse({
            message: "Sign-in log retrieved successfully",
            data: result
        })
    );
}
