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
  async getMapLocations(req: Request, res: Response) {
    const { lat, lng, radius, categoryId } = req.query as any;

    const categoryFilter = categoryId ? `AND "categoryId" = ${categoryId}` : "";
    const locations = await prisma.$queryRawUnsafe(
      `
      SELECT 
        id, 
        name, 
        "categoryId",
        "priceLevel",
        ST_X(location::geometry) as longitude,
        ST_Y(location::geometry) as latitude,
        ST_Distance(
          location, 
          ST_SetSRID(ST_MakePoint($1, $2), 4326)::geography
        ) as distance
      FROM "Locations"
      WHERE ST_DWithin(
        location, 
        ST_SetSRID(ST_MakePoint($1, $2), 4326)::geography, 
        $3
      )
      ${categoryFilter}
      AND "isDeleted" = false
      ORDER BY distance ASC
      LIMIT 100
    `,
      lng,
      lat,
      radius,
    );
    return res.status(200).json(createResponse({ data: locations }));
  },
};
