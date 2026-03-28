import axios from "axios";
import { Request, Response } from "express";
import { prisma } from "../../libs/prisma";
import bcrypt from "bcrypt";
import { createResponse } from "../../utils/response";
import { userRole } from "../../generated/prisma/browser";
import { is } from "zod/v4/locales";

//MISSING IS INACTIVE LOGIC
export const getList = async (req: Request, res: Response) => {
    const {usernameOrEmail = "", sortBy = "username", sortOrder = "asc"} = req.query;
    const roleParam = req.query.role as string;
    const isBanned = req.query.isBanned === 'true'; 
    const isDeleted = req.query.isDeleted === 'true';
    const isInactive = req.query.isInactive === 'true';

    const roles = roleParam 
        ? roleParam.split(",").map(r => r.trim().toLowerCase())
        : ['user', 'moderator', 'admin'];
    const users = await prisma.user.findMany({
        where: {
            AND: [
                {
                    OR: [
                        { username: { contains: String(usernameOrEmail), mode: 'insensitive' } },
                        { email: { contains: String(usernameOrEmail), mode: 'insensitive' } },
                    ]
                },
                {isDeleted: isDeleted},
                {isBanned: isBanned},
                {role: { in: roles as userRole[] }}
                ]
        },
        orderBy: {
            [String(sortBy)]: sortOrder as 'asc' | 'desc'
        }
    });
    return res.status(200).json(
        createResponse({
            message: "Users retrieved successfully",
            data: users
        })
    );
}

export const toggleBan = async (req: Request, res: Response) => {
    const {id} = req.params;
    const {ban} = req.body;

    const updatedUser = await prisma.user.update({
        where: { id: String(id)},
        data: {isBanned: Boolean(ban)}
    });

    return res.status(200).json(
        createResponse({
            message: ban ? "User banned successfully" : "User unbanned successfully",
            data: updatedUser
        })
    );
}

export const toggleSoftDeleteUser = async (req: Request, res: Response) => {
    const {id} = req.params;
    const {delete: isDeleted} = req.body;
    console.log(isDeleted);

    const updatedUser = await prisma.user.update({
        where: { id: String(id)},
        data: {isDeleted: Boolean(isDeleted)}
    });

    return res.status(200).json(
        createResponse({
            message: isDeleted ? "User soft-deleted successfully" : "User un-soft-deleted successfully",
            data: updatedUser
        })
    );
}

export const updatePassword = async (req: Request, res: Response) => {
    const {id} = req.params;
    const {newPassword} = req.body;

    const User = await prisma.user.findUnique({
        where: {id: String(id)},
        include: {authProviders: true}
    });

    if (!User) {
        return res.status(404).json(
            createResponse({
                message: "User not found"
            })
        );
    }

    const localAuth = User.authProviders.find((p) => p.provider === "local");
    if (!localAuth) {
      return res.status(400).json(
        createResponse({
          message: "Bad request",
          error: "Account uses social login",
        }),
      );
    }

    await prisma.authProvider.update({
        where: {id: String(localAuth?.id)},
        data: {
            hashPassword: await bcrypt.hash(newPassword, 10)
        }
    });

    return res.status(200).json(
        createResponse({
            message: "Password updated successfully"
        })
    ); 
}

export const demoteFromModerator = async (req: Request, res: Response) => {
    const {id} = req.params;

    const updatedUser = await prisma.user.findUnique({
        where: { id: String(id)},
    });

    if(!updatedUser) {
        return res.status(404).json(
            createResponse({
                message: "User not found"
            })
        );
    }

    if (updatedUser.role !== userRole.moderator) {
        return res.status(400).json(
            createResponse({
                message: "User is not a moderator"
            })
        );
    }

    const demotedUser = await prisma.user.update({
        where: { id: String(id)},
        data: {role: userRole.user}
    });

    return res.status(200).json(
        createResponse({
            message: "User demoted from moderator successfully",
            data: demotedUser
        })
    );

}