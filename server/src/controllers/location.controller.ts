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

    return res.status(200).json(createResponse({ data: location }));
  },

  async update(req: Request, res: Response) {
    const { id } = req.params as { id: string };
    const { latitude, longitude, ...rest } = req.body;
    const updatedLocation = await prisma.$transaction(async (tx) => {
      const location = await tx.location.update({
        where: { id: id },
        data: {
          ...rest,
          updatedAt: new Date(),
        },
      });

      if (latitude !== undefined && longitude !== undefined) {
        await tx.$executeRawUnsafe(
          `UPDATE "Location" 
         SET location = ST_SetSRID(ST_MakePoint($1, $2), 4326)::geography 
         WHERE id = $3`,
          longitude,
          latitude,
          id,
        );
      }

      return location;
    });

    return res.status(200).json(
      createResponse({
        message: "",
        data: updatedLocation,
      }),
    );
  },
};
