import { Request, Response } from "express";
import { createResponse } from "../utils/response";
import { prisma } from "../libs/prisma";
import ApiError from "../utils/apiError";

export const locationController = {
  async getById(req: Request, res: Response) {
    const { id } = req.params as { id: string };

    const location = prisma.location.findUnique({
      where: { id: id },
    });

    if (!location) {
      throw new ApiError(404, "Location not found");
    }

    return res.json(createResponse({ data: location }));
  },

  async update(req: Request, res: Response) {
    const {id} = req.params as {id:string};
    const updateData = req.body;

    const updatedLocation = await prisma.location.update({
        where: { id },
        data: updateData,
    });

    return res.json(createResponse({
        message: "Location updated successfully",
        data: updatedLocation
    }));
  }
};
