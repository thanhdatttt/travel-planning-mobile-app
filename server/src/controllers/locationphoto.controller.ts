import { Request, Response } from "express";
import { createResponse } from "../utils/response";
import { prisma } from "../libs/prisma";
import ApiError from "../utils/apiError";

export const locationPhotoController = {
  async uploadPhoto(req: Request, res: Response) {
    if (!req.file) throw new ApiError(400, "No file uploaded");

    const imageUrl = (req.file as any).path;
    const { locationId } = req.params as { locationId: string };
    const uploaderId = req.user.id as string;

    const location = await prisma.location.findUnique({
      where: { id: locationId },
    });
    if (!location) throw new ApiError(404, "Location not found");

    const newPhoto = await prisma.locationPhoto.create({
      data: {
        url: imageUrl,
        locationId: locationId,
        uploaderId: uploaderId,
      },
    });

    return res.status(201).json(
      createResponse({
        message: "Upload photo successfully",
        data: newPhoto,
      }),
    );
  },
};
