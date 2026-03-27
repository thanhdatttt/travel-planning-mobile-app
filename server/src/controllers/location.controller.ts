import { Request, Response } from "express";
import { createResponse } from "../utils/response";
import { prisma } from "../libs/prisma";
import ApiError from "../utils/apiError";

export const locationController = {
  async getById(req: Request, res: Response) {
    const { id } = req.params;

    const locations: any[] = await prisma.$queryRawUnsafe(
      `
      SELECT 
        id, "osmId", name, slug, description, address, phone, website, 
        "avgRating", "ratingCount", "priceLevel", metadata, "categoryId",
        "createdAt", "updatedAt",
        ST_X(location::geometry) as longitude, 
        ST_Y(location::geometry) as latitude
      FROM "Location"
      WHERE id = $1
      LIMIT 1
    `,
      id,
    );

    const location = locations[0];

    if (!location) {
      return res
        .status(404)
        .json(createResponse({ message: "Location not found." }));
    }

    const photos = await prisma.locationPhoto.findMany({
      where: { locationId: id as string },
    });

    return res
      .status(200)
      .json(createResponse({ data: { ...location, photos } }));
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
      FROM "Location"
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
  async search(req: Request, res: Response) {
    const q = req.query.q as string;
    const categoryId = req.query.categoryId
      ? Number(req.query.categoryId)
      : undefined;
    const priceLevel = req.query.priceLevel
      ? Number(req.query.priceLevel)
      : undefined;

    const page = Math.max(1, Number(req.query.page) || 1);
    const limit = Math.max(1, Number(req.query.limit) || 10);
    const skip = (page - 1) * limit;

    const where: any = {
      isDeleted: false,
    };

    if (q) {
      where.OR = [
        { name: { contains: q, mode: "insensitive" } },
        { address: { contains: q, mode: "insensitive" } },
      ];
    }

    if (categoryId) {
      where.categoryId = categoryId;
    }

    if (priceLevel) {
      where.priceLevel = priceLevel;
    }

    const [locations, total] = await prisma.$transaction([
      prisma.location.findMany({
        where,
        skip,
        take: limit,
        orderBy: { avgRating: "desc" },
        include: {
          category: true,
          locationPhotos: {
            take: 1,
          },
        },
      }),
      prisma.location.count({ where }),
    ]);

    return res.status(200).json(
      createResponse({
        data: {
          items: locations,
          meta: {
            total,
            page,
            limit,
            totalPages: Math.ceil(total / limit),
          },
        },
        message: "Search successfully.",
      }),
    );
  },
};
