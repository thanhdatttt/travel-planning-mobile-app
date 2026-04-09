import { Request, Response } from "express";
import { createResponse } from "../utils/response";
import { prisma } from "../libs/prisma";
import ApiError from "../utils/apiError";

export const userController = {
  async getMe(req: Request, res: Response) {
    return res
      .status(200)
      .json(
        createResponse({ message: "Get user successfully", data: req.user }),
      );
  },

  async updateUser(req: Request, res: Response) {
    const updatedUser = await prisma.user.update({
      where: { id: req.user.id },
      data: req.body,
    });

    return res.status(200).json(
      createResponse({
        message: "Update user successfully",
        data: updatedUser,
      }),
    );
  },

  async uploadAvatar(req: Request, res: Response) {
    const imageUrl = (req.file as any).path;
    const id = req.user.id as string;

    const updatedUser = await prisma.user.update({
      where: { id: id },
      data: { avatarUrl: imageUrl },
    });

    return res.status(200).json(
      createResponse({
        message: "Upload avatar successfully",
        data: updatedUser,
      }),
    );
  },
};
