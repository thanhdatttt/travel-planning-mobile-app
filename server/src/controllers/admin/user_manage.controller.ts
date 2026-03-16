import axios from "axios";
import { Request, Response } from "express";
import { prisma } from "../../libs/prisma";
import { config } from "../../configs/config";
import { createResponse } from "../../utils/response";

export const getList = async (req: Request, res: Response) => {
    const {fullName = "", email = "", role = "", isBanned = false, sortBy = "name", sortOrder = "asc"} = req.query;
    
    const where: any = {
        AND: [
            {
                OR: [
                    { name: { contains: String(fullName), mode: 'insensitive' } },
                    { email: { contains: String(email), mode: 'insensitive' } },
                ]
            }
        ]
    };

    if (role) {
        where.AND.push({ role: String(role) });
    }

    // if (isBanned !== undefined) {
    //     const bannedBool = isBanned === 'true' || isBanned === true;
    //     where.AND.push({ isBanned: bannedBool });
    // }

    const users = await prisma.user.findMany({
        where,
        orderBy: {
            [String(sortBy)]: sortOrder as 'asc' | 'desc'
        },
        select: {
            id: true,
            fullName: true,
            email: true,
            role: true,
            // isBanned: true,
            createdAt: true,
        }
    });

    return res.status(200).json(
        createResponse({
            message: "Users retrieved successfully",
            data: users
        })
    );
}