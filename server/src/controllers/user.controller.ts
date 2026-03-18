import { Request, Response } from "express";
import { createResponse } from "../utils/response";
import { prisma } from "../libs/prisma";

export const getMe = async (req: Request & { user?: any }, res: Response) => {
  try {
    return res
      .status(200)
      .json(
        createResponse({ message: "Get user successfully", data: req.user }),
      );
  } catch (err: any) {
    console.log("Error when getting user info: ", err.message);
    return res
      .status(500)
      .json(createResponse({ message: "System error", error: err.message }));
  }
};

export const updateUser = async (req: Request, res: Response) => {
  try {
    const {
      email,
      username,
      fullName,
      phone,
      address,
      avatarUrl,
      bio,
      preference,
    } = req.body;

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
  } catch (err: any) {
    console.log("Error when updating user info: ", err.message);
    return res
      .status(500)
      .json(createResponse({ message: "System error", error: err.message }));
  }
};
