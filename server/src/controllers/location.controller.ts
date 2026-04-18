import { Request, Response } from "express";
import { createResponse } from "../utils/response";
import { prisma } from "../libs/prisma";

export const locationController = {
  async getById(req: Request, res: Response) {
    const { id } = req.params;

    const locations: any[] = await prisma.$queryRawUnsafe(
      `
      SELECT 
        l.id, l."osmId", l.name, l.slug, l.description, l.address, l.phone, 
        l.website, l.email, l."priceLevel", l.metadata, 
        c.id as "catId", c.slug as "catSlug", l."createdAt", l."updatedAt",
        ST_X(l.location::geometry) as longitude, 
        ST_Y(l.location::geometry) as latitude,
        
        COALESCE((
          SELECT ROUND(AVG(r.rating)::numeric, 1) 
          FROM "Review" r 
          WHERE r."locationId" = l.id
        ), 0)::float as "avgRating",
        
        (
          SELECT COUNT(*)::int 
          FROM "Review" r 
          WHERE r."locationId" = l.id
        ) as "ratingCount"

      FROM "Location" l
      INNER JOIN "LocationCategory" c ON l."categoryId" = c.id
      WHERE l.id = $1
      LIMIT 1
    `,
      id,
    );

    const location = locations[0];
    const { catId, catSlug, ...locationData } = location;

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

    return res.status(200).json(
      createResponse({
        data: {
          ...locationData,
          category: {
            id: catId,
            slug: catSlug,
          },
          photos,
          opening_hours,
        },
      }),
    );
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
          
          COALESCE((
            SELECT ROUND(AVG(r.rating)::numeric, 1) 
            FROM "Review" r 
            WHERE r."locationId" = l.id
          ), 0)::float as "avgRating",
          
          (
            SELECT COUNT(*)::int 
            FROM "Review" r 
            WHERE r."locationId" = l.id
          ) as "ratingCount",

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

    let whereClause = `WHERE l."isDeleted" = false`;
    const whereParams: any[] = [];
    let paramIndex = 1;

    if (q) {
      whereParams.push(`%${q}%`);
      whereClause += ` AND (l.name ILIKE $${paramIndex} OR l.address ILIKE $${paramIndex})`;
      paramIndex++;
    }

    if (categoryId) {
      whereParams.push(categoryId);
      whereClause += ` AND l."categoryId" = $${paramIndex}`;
      paramIndex++;
    }

    if (priceLevel) {
      whereParams.push(priceLevel);
      whereClause += ` AND l."priceLevel" = $${paramIndex}`;
      paramIndex++;
    }

    let orderClause = `ORDER BY "calculatedAvgRating" DESC`;
    if (sortBy === "priceLevel") {
      orderClause = `ORDER BY l."priceLevel" ${sortOrder === "asc" ? "ASC" : "DESC"}`;
    } else if (sortBy === "avgRating") {
      orderClause = `ORDER BY "calculatedAvgRating" ${sortOrder === "asc" ? "ASC" : "DESC"}`;
    }

    try {
      const countQuery = `SELECT COUNT(*)::int as total FROM "Location" l ${whereClause}`;
      const countResult: any[] = await prisma.$queryRawUnsafe(
        countQuery,
        ...whereParams,
      );
      const total = countResult[0].total;

      const queryParams = [...whereParams, limit, skip];
      const limitIndex = paramIndex;
      const skipIndex = paramIndex + 1;

      const mainQuery = `
        SELECT 
          l.*,
          ST_X(l.location::geometry) as longitude, 
          ST_Y(l.location::geometry) as latitude,
          
          row_to_json(c) as category,
          
          COALESCE((
            SELECT ROUND(AVG(r.rating)::numeric, 1) 
            FROM "Review" r 
            WHERE r."locationId" = l.id
          ), 0)::float as "calculatedAvgRating",
          
          (
            SELECT COUNT(*)::int 
            FROM "Review" r 
            WHERE r."locationId" = l.id
          ) as "ratingCount",
          
          -- Lấy tối đa 1 bức ảnh (giống logic take: 1 cũ)
          COALESCE((
            SELECT json_agg(row_to_json(lp))
            FROM (
              SELECT * FROM "LocationPhoto" 
              WHERE "locationId" = l.id 
              LIMIT 1
            ) lp
          ), '[]'::json) as photos

        FROM "Location" l
        LEFT JOIN "LocationCategory" c ON l."categoryId" = c.id
        ${whereClause}
        ${orderClause}
        LIMIT $${limitIndex} OFFSET $${skipIndex}
      `;

      const locations: any[] = await prisma.$queryRawUnsafe(
        mainQuery,
        ...queryParams,
      );

      const formattedLocations = locations.map((loc: any) => {
        delete loc.location;
        const locationPhotos = loc.photos || [];

        const realAvgRating = loc.calculatedAvgRating;
        delete loc.calculatedAvgRating;

        return {
          ...loc,
          avgRating: realAvgRating,
          photos: locationPhotos,
          imageUrl: locationPhotos.length > 0 ? locationPhotos[0].url : null,
        };
      });

      console.log("Formatted Locations:", formattedLocations);

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
    } catch (error) {
      console.error("Lỗi khi search location:", error);
      return res
        .status(500)
        .json(createResponse({ message: "Internal Server Error" }));
    }
  },
  async getAll(req: Request, res: Response) {},
};
