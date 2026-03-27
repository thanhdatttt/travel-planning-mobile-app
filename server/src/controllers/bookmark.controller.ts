import { Request, Response } from "express";
import { createResponse } from "../utils/response";
import { prisma } from "../libs/prisma";
import ApiError from "../utils/apiError";

export const bookmarkController = {
  async toggle(req: Request, res: Response) {
    const userId = req.user.id;
    const { locationId } = req.body;

    const existingBookmark = await prisma.bookmark.findUnique({
      where: { userId_locationId: { userId, locationId } },
    });

    if (existingBookmark) {
      await prisma.bookmark.delete({
        where: { id: existingBookmark.id },
      });
      return res.json(createResponse({ message: "Bookmark removed" }));
    }

    const bookmark = await prisma.bookmark.create({
      data: { userId, locationId },
    });

    return res.status(201).json(
      createResponse({
        message: "Location bookmarked successfully",
        data: bookmark,
      }),
    );
  },

  async getAll(req: Request, res: Response) {
    const userId = req.user.id;
    const page = Number(req.query.page) || 1;
    const limit = Number(req.query.limit) || 10;
    const skip = (page - 1) * limit;

    const [bookmarks, total] = await Promise.all([
      prisma.bookmark.findMany({
        where: { userId },
        skip,
        take: limit,
        // include: { location: true }, // might need location details
        orderBy: { createdAt: "desc" },
      }),
      prisma.bookmark.count({ where: { userId } }),
    ]);

    return res.json(
      createResponse({
        data: bookmarks,
        metadata: { total, page, lastPage: Math.ceil(total / limit) },
      }),
    );
  },

  async delete(req: Request, res: Response) {
    const { id } = req.params as { id: string };
    const userId = req.user.id;

    const bookmark = await prisma.bookmark.findUnique({ where: { id } });

    if (!bookmark) throw new ApiError(404, "Bookmark not found");
    if (bookmark.userId !== userId) throw new ApiError(403, "Unauthorized");

    await prisma.bookmark.delete({ where: { id } });

    return res.json(
      createResponse({ message: "Bookmark deleted successfully" }),
    );
  },
};
