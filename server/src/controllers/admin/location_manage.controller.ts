import axios from "axios";
import { Request, Response } from "express";
import { prisma } from "../../libs/prisma";
import { config } from "../../configs/config";
import { createResponse } from "../../utils/response";
import { locationType, userRole } from "../../generated/prisma/browser";
import { equal } from "node:assert";

const MAX_VALUE = 32767;

export const getList = async (req: Request, res: Response) => {
    const {name = "", sortBy = "name", sortOrder = "asc", minPrice = 0, maxPrice = MAX_VALUE, 
        minDistance = 0, maxDistance = MAX_VALUE, minRating = 0, maxRating = 10} = req.query;
    const type = req.query.type as string;

    const locations = await prisma.location.findMany({
        where: {
            AND: [
                { name: { contains: String(name), mode: 'insensitive' } },
                { priceLevel: {gte: Number(minPrice)}},
                { priceLevel: {lte: Number(maxPrice)}},
                { avgRating: {gte: Number(minRating)}},
                { avgRating: {lte: Number(maxRating)}},
                { type: type?.toLowerCase() === "attraction" ? locationType.attraction : locationType.restaurant}
            ]},
        orderBy: {
            [String(sortBy)]: sortOrder
        }
        });

        return res.status(200).json(
            createResponse({
                message: "Locations retrieved successfully",
                data: locations
            })
        );
}

export const adminUpdate = async (req: Request, res: Response) {
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
  }