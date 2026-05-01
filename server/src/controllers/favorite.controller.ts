import { Request, Response } from "express";
import { createResponse } from "../utils/response";
import { prisma } from "../libs/prisma";
import ApiError from "../utils/apiError";

export const favoriteController = {
  async toggle(req: Request, res: Response) {
    const userId = req.user.id;
    const { itineraryId } = req.body;

    const existingFavorite = await prisma.favorite.findUnique({
      where: {
        userId_itineraryId: { userId, itineraryId },
      },
    });

    if (existingFavorite) {
      await prisma.favorite.delete({
        where: { id: existingFavorite.id },
      });
      return res.json(createResponse({ message: "Removed from favorites" }));
    }

    const favorite = await prisma.favorite.create({
      data: { userId, itineraryId },
    });

    return res.status(201).json(
      createResponse({
        message: "Itinerary favorited",
        data: favorite,
      }),
    );
  },

  async getAll(req: Request, res: Response) {
    const userId = req.user.id;
    const page = Number(req.query.page) || 1;
    const limit = Number(req.query.limit) || 10;
    const skip = (page - 1) * limit;
    const offset = (page - 1) * limit;

    try {
      const favorites = await prisma.$queryRaw`
      SELECT 
        f.id, 
        f."userId", 
        f."itineraryId", 
        f."createdAt",
        i.title as "itineraryTitle",
        i.description as "itineraryDescription",
        -- Subquery lấy URL ảnh của địa điểm có orderIdx nhỏ nhất (đầu tiên)
        (
          SELECT lp.url 
          FROM "ItineraryItem" ii
          JOIN "LocationPhoto" lp ON ii."locationId" = lp."locationId"
          WHERE ii."itineraryId" = f."itineraryId"
          ORDER BY lp."isFeature" DESC, ii."orderIdx" ASC
          LIMIT 1
        ) as "imageUrl"
      FROM "Favorite" f
      JOIN "Itinerary" i ON f."itineraryId" = i.id
      WHERE f."userId" = ${userId}
      ORDER BY f."createdAt" DESC
      LIMIT ${limit} OFFSET ${offset}
    `;

      const total: any = await prisma.$queryRaw`
      SELECT COUNT(*) FROM "Favorite" WHERE "userId" = ${userId}
    `;

      return res.json(
        createResponse({
          data: favorites,
          metadata: {
            total: Number(total[0].count),
            page,
            lastPage: Math.ceil(Number(total[0].count) / limit),
          },
        }),
      );
    } catch (error) {
      res.status(500).json({ message: "Raw query error", error });
    }
  },

  async delete(req: Request, res: Response) {
    const { id } = req.params as { id: string };
    const userId = req.user.id;

    const favorite = await prisma.favorite.findUnique({ where: { id } });

    if (!favorite) throw new ApiError(404, "Favorite not found");
    if (favorite.userId !== userId) throw new ApiError(403, "Unauthorized");

    await prisma.favorite.delete({ where: { id } });

    return res.json(createResponse({ message: "Favorite deleted" }));
  },

  async checkStatus(req: Request, res: Response) {
    const { itineraryId } = req.query as { itineraryId: string };
    const userId = req.user.id;

    const favorite = await prisma.favorite.findUnique({
      where: { userId_itineraryId: { userId, itineraryId } },
    });

    return res.json(createResponse({ data: !!favorite }));
  },
};
