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

    try {
      // 1. Đếm tổng số bookmark để phân trang
      const countResult: any[] = await prisma.$queryRawUnsafe(
        `SELECT COUNT(*)::int as total FROM "Bookmark" WHERE "userId" = $1`,
        userId,
      );
      const total = countResult[0].total;

      // 2. Query Raw SQL y hệt như Search, nhưng select thêm id của Bookmark
      const rawData: any[] = await prisma.$queryRawUnsafe(
        `
        SELECT 
          b.id as "bookmarkId", 
          b."userId", 
          b."locationId", 
          b."createdAt" as "bookmarkCreatedAt",
          l.*,
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
          ) as "ratingCount",
          
          COALESCE((
            SELECT json_agg(row_to_json(lp))
            FROM (SELECT * FROM "LocationPhoto" WHERE "locationId" = l.id LIMIT 1) lp
          ), '[]'::json) as photos

        FROM "Bookmark" b
        INNER JOIN "Location" l ON b."locationId" = l.id
        WHERE b."userId" = $1
        ORDER BY b."createdAt" DESC
        LIMIT $2 OFFSET $3
      `,
        userId,
        limit,
        skip,
      );

      // 3. Format lại dữ liệu BỌC Location bên trong Bookmark (Đúng chuẩn Mobile cần)
      const formattedBookmarks = rawData.map((row: any) => {
        const locationPhotos = row.photos || [];

        // Trích xuất các trường của Location
        const locationData = {
          ...row,
          photos: locationPhotos,
          imageUrl: locationPhotos.length > 0 ? locationPhotos[0].url : null,
        };

        // Xóa các trường của Bookmark và Raw data rác ra khỏi object Location
        delete locationData.bookmarkId;
        delete locationData.userId;
        delete locationData.bookmarkCreatedAt;
        delete locationData.location;

        // Trả về đúng cấu trúc BookmarkResponse mà Mobile đang đợi
        return {
          id: row.bookmarkId,
          userId: row.userId,
          locationId: row.locationId,
          createdAt: row.bookmarkCreatedAt,
          location: locationData,
        };
      });

      return res.json(
        createResponse({
          data: formattedBookmarks,
          metadata: { total, page, lastPage: Math.ceil(total / limit) },
        }),
      );
    } catch (error) {
      console.error("Lỗi khi lấy danh sách bookmark:", error);
      return res
        .status(500)
        .json(createResponse({ message: "Internal Server Error" }));
    }
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

  async getByLocationId(req: Request, res: Response) {
    const { locationId } = req.query as { locationId: string };
    const userId = req.user.id;

    const bookmark = await prisma.bookmark.findUnique({
      where: { userId_locationId: { userId, locationId } },
    });

    return res.json(createResponse({ data: !!bookmark }));
  },

  async getAllByUserId(req: Request, res: Response) {
    const userId = req.user.id;
    const page = Number(req.query.page) || 1;
    const limit = Number(req.query.limit) || 10;
    const skip = (page - 1) * limit;

    try {
      const locations: any[] = await prisma.$queryRawUnsafe(
        `
        SELECT 
          l.*,
          ST_X(l.location::geometry) as longitude, 
          ST_Y(l.location::geometry) as latitude,
          
          -- Tính trực tiếp avgRating từ bảng Review
          COALESCE((
            SELECT ROUND(AVG(r.rating)::numeric, 1) 
            FROM "Review" r 
            WHERE r."locationId" = l.id
          ), 0)::float as "avgRating",
          
          -- Đếm số lượng đánh giá
          (
            SELECT COUNT(*)::int 
            FROM "Review" r 
            WHERE r."locationId" = l.id
          ) as "ratingCount",
          
          -- Lấy 1 bức ảnh làm cover
          COALESCE((
            SELECT json_agg(row_to_json(lp))
            FROM (
              SELECT * FROM "LocationPhoto" 
              WHERE "locationId" = l.id 
              LIMIT 1
            ) lp
          ), '[]'::json) as photos

        FROM "Bookmark" b
        INNER JOIN "Location" l ON b."locationId" = l.id
        WHERE b."userId" = $1
        ORDER BY b."createdAt" DESC
        LIMIT $2 OFFSET $3
      `,
        userId,
        limit,
        skip,
      );

      const formattedLocations = locations.map((loc: any) => {
        delete loc.location;

        const locationPhotos = loc.photos || [];

        return {
          ...loc,
          photos: locationPhotos,
          imageUrl: locationPhotos.length > 0 ? locationPhotos[0].url : null,
        };
      });

      return res.status(200).json(createResponse({ data: formattedLocations }));
    } catch (error) {
      console.error("Lỗi khi lấy danh sách bookmark:", error);
      return res
        .status(500)
        .json(createResponse({ message: "Internal Server Error" }));
    }
  },
};
