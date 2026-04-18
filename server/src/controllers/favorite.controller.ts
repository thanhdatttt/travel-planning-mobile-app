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

    try {
      const [favorites, total] = await Promise.all([
        prisma.favorite.findMany({
          where: { userId },
          skip,
          take: limit,
          orderBy: { createdAt: "desc" },
          include: {
            itinerary: true,
          },
        }),
        prisma.favorite.count({ where: { userId } }),
      ]);

      const formattedFavorites = favorites.map((fav: any) => {
        const itinerary = fav.itinerary;

        let finalImageUrl = itinerary.imageUrl || null;

        if (
          !finalImageUrl &&
          itinerary.itineraryPhotos &&
          itinerary.itineraryPhotos.length > 0
        ) {
          finalImageUrl = itinerary.itineraryPhotos[0].url;
        }

        return {
          id: fav.id,
          userId: fav.userId,
          itineraryId: fav.itineraryId,
          createdAt: fav.createdAt,
          itinerary: {
            ...itinerary,
            imageUrl: finalImageUrl,
          },
        };
      });

      return res.json(
        createResponse({
          data: formattedFavorites,
          metadata: { total, page, lastPage: Math.ceil(total / limit) },
        }),
      );
    } catch (error) {
      console.error("Lỗi khi lấy favorite:", error);
      return res
        .status(500)
        .json(createResponse({ message: "Internal Server Error" }));
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
