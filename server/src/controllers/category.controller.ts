import { Request, Response } from "express";
import { createResponse } from "../utils/response";
import { prisma } from "../libs/prisma";
import ApiError from "../utils/apiError";

export const categoryController = {
  async getCategories(req: Request, res: Response) {
    const locationCategories = await prisma.locationCategory.findMany({
      orderBy: {
        displayOrder: "asc",
      },
    });

    return res.status(200).json(
      createResponse({
        data: locationCategories,
        message: "Get categories successfully.",
      }),
    );
  },
};
