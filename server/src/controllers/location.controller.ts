import { Request, Response } from "express";
import { createResponse } from "../utils/response";
import { prisma } from "../libs/prisma";

export const locationController = {
  async getById(req: Request, res: Response) {
    const { id } = req.params;

    const locations: any[] = await prisma.$queryRawUnsafe(
      `
      SELECT 
        id, "osmId", name, slug, description, address, phone, website, email, 
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

    const opening_hours = await prisma.locationHour.findMany({
      where: { locationId: id as string },
      orderBy: { dayOfWeek: "asc" }, //keeps them in order (Mon-Sun)
    });

    return res
      .status(200)
      .json(createResponse({ data: { ...location, photos, opening_hours } }));
  },
  async getMapLocations(req: Request, res: Response) {
    const { lat, lng, radius, categoryId } = req.query as any;

    const latitude = parseFloat(lat);
    const longitude = parseFloat(lng);
    const rad = parseInt(radius) || 5000000;

    const categoryFilter = categoryId
      ? `AND l."categoryId" = ${parseInt(categoryId)}`
      : "";

    const locations: any[] = await prisma.$queryRawUnsafe(
      `
        SELECT 
          l.id, 
          l.name, 
          l.address,
          l."categoryId",
          l."priceLevel",
          l."avgRating",
          l."ratingCount",
          ST_X(l.location::geometry) as longitude,
          ST_Y(l.location::geometry) as latitude,
          ST_Distance(
            l.location, 
            ST_SetSRID(ST_MakePoint($1, $2), 4326)::geography
          ) as distance,
          json_build_object(
            'nameVi', c."nameVi", 
            'icon', c.icon
          ) as category,
          -- SỬA Ở ĐÂY: Đổi alias thành "photos"
          COALESCE(
            (
              SELECT json_agg(json_build_object('url', lp.url))
              FROM "LocationPhoto" lp
              WHERE lp."locationId" = l.id
            ), 
            '[]'::json
          ) as photos
        FROM "Location" l
        LEFT JOIN "LocationCategory" c ON l."categoryId" = c.id
        WHERE ST_DWithin(
          l.location, 
          ST_SetSRID(ST_MakePoint($1, $2), 4326)::geography, 
          $3
        )
        ${categoryFilter}
        AND l."isDeleted" = false
        ORDER BY distance ASC
        `,
      longitude,
      latitude,
      rad,
    );

    const formattedLocations = locations.map((loc: any) => {
      return {
        ...loc,
        imageUrl:
          loc.photos && loc.photos.length > 0 ? loc.photos[0].url : null,
      };
    });

    return res.status(200).json({
      message: "Get nearby locations successfully.",
      data: formattedLocations,
    });
  },
  async search(req: Request, res: Response) {
    const q = req.query.q as string;
    const categoryId = req.query.categoryId
      ? Number(req.query.categoryId)
      : undefined;
    const priceLevel = req.query.priceLevel
      ? Number(req.query.priceLevel)
      : undefined;

    const sortBy = req.query.sortBy as string;
    const sortOrder = req.query.sortOrder as string;

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

    let orderByCondition: any = { avgRating: "desc" };

    if (sortBy === "priceLevel") {
      orderByCondition = { priceLevel: sortOrder === "asc" ? "asc" : "desc" };
    } else if (sortBy === "avgRating") {
      orderByCondition = { avgRating: sortOrder === "asc" ? "asc" : "desc" };
    }

    const [locations, total] = await prisma.$transaction([
      prisma.location.findMany({
        where,
        skip,
        take: limit,
        orderBy: orderByCondition,
        include: {
          category: true,
          locationPhotos: {
            take: 1,
          },
          _count: {
            select: {
              reviews: true,
            },
          },
        },
      }),
      prisma.location.count({ where }),
    ]);

    let coordinates: any[] = [];
    if (locations.length > 0) {
      const idsString = locations.map((loc) => `'${loc.id}'`).join(",");

      coordinates = await prisma.$queryRawUnsafe(`
        SELECT 
          id, 
          ST_X(location::geometry) as longitude, 
          ST_Y(location::geometry) as latitude
        FROM "Location"
        WHERE id IN (${idsString})
      `);
    }

    const formattedLocations = locations.map((loc) => {
      const { _count, locationPhotos, ...rest } = loc as any;

      const coord = coordinates.find((c: any) => c.id === loc.id);

      return {
        ...rest,
        ratingCount: _count?.reviews || 0,
        latitude: coord ? coord.latitude : null,
        longitude: coord ? coord.longitude : null,

        photos: locationPhotos || [],

        imageUrl:
          locationPhotos && locationPhotos.length > 0
            ? locationPhotos[0].url
            : null,
      };
    });

    return res.status(200).json(
      createResponse({
        data: {
          items: formattedLocations,
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

  async getAll(req: Request, res: Response) {},
};
