import { prisma } from "../libs/prisma";
import { GoogleGenerativeAI } from "@google/generative-ai";

const genAI = new GoogleGenerativeAI(process.env.GEMINI_API_KEY || "");
const model = genAI.getGenerativeModel({
  model: "gemini-3.1-flash-lite-preview",
  generationConfig: {
    responseMimeType: "application/json",
    maxOutputTokens: 1000,
  },
});

export class RecommendationUtils {
  public static async getSmartRecommendations(
    userId: string,
    lat: number,
    lng: number,
  ) {
    try {
      const [geoCandidates, contentCandidates] = await Promise.all([
        this.getLayer1NearestCandidates(lat, lng, 30),
        this.getLayer2ContentCandidates(userId, 30),
      ]);

      const candidateMap = new Map();
      [...geoCandidates, ...contentCandidates].forEach((loc) => {
        candidateMap.set(loc.id, loc);
      });
      const candidatePool = Array.from(candidateMap.values());

      if (candidatePool.length === 0) return [];

      const aiSelectedIds = await this.getLayer3AILogic(candidatePool);

      return await this.getDetailedLocations(aiSelectedIds, lat, lng);
    } catch (error) {
      console.error("Error calling AI Recommendation:", error);

      const fallback = await this.getLayer1NearestCandidates(lat, lng, 10);
      const fallbackIds = fallback.map((l) => l.id);
      return await this.getDetailedLocations(fallbackIds, lat, lng);
    }
  }

  private static async getLayer1NearestCandidates(
    lat: number,
    lng: number,
    limit: number,
  ) {
    const query = `
      SELECT id, name, "categoryId", "avgRating"
      FROM "Location"
      WHERE "isDeleted" = false
      ORDER BY location <-> ST_SetSRID(ST_MakePoint(${lng}, ${lat}), 4326)
      LIMIT ${limit};
    `;
    return await prisma.$queryRawUnsafe<any[]>(query);
  }

  private static async getLayer2ContentCandidates(
    userId: string,
    totalLimit: number,
  ) {
    const topCategoriesQuery = `
      WITH UserLocations AS (
        SELECT l."categoryId" FROM "Bookmark" b JOIN "Location" l ON b."locationId" = l.id WHERE b."userId" = $1
        UNION ALL
        SELECT l."categoryId" FROM "Favorite" f JOIN "ItineraryItem" ii ON f."itineraryId" = ii."itineraryId" JOIN "Location" l ON ii."locationId" = l.id WHERE f."userId" = $1
      )
      SELECT "categoryId", COUNT(*) as count FROM UserLocations GROUP BY "categoryId" ORDER BY count DESC LIMIT 3;
    `;

    const topCategories = await prisma.$queryRawUnsafe<any[]>(
      topCategoriesQuery,
      userId,
    );
    if (topCategories.length === 0) return [];

    const bookmarked = await prisma.bookmark.findMany({
      where: { userId },
      select: { locationId: true },
    });
    const excludedIds = bookmarked.map((b) => b.locationId);

    const limits =
      topCategories.length === 3
        ? [15, 10, 5]
        : topCategories.length === 2
          ? [18, 12]
          : [totalLimit];

    const contentPromises = topCategories.map((cat, index) => {
      return prisma.location.findMany({
        where: {
          categoryId: cat.categoryId,
          isDeleted: false,
          id: { notIn: excludedIds },
        },
        orderBy: { avgRating: "desc" },
        take: limits[index] ?? 10,
        select: { id: true, name: true, categoryId: true, avgRating: true },
      });
    });

    const contentResults = await Promise.all(contentPromises);
    return contentResults.flat();
  }

  private static async getLayer3AILogic(
    candidatePool: any[],
  ): Promise<string[]> {
    const miniCandidates = candidatePool.map((c) => ({
      id: c.id,
      name: c.name,
      rating: c.avgRating,
    }));

    const prompt = `
      Bạn là chuyên gia lọc dữ liệu du lịch.
      Danh sách địa điểm: ${JSON.stringify(miniCandidates)}

      Nhiệm vụ: Chọn ra ĐÚNG 10 ID địa điểm từ danh sách trên sao cho đảm bảo tính đa dạng và chất lượng (rating).
      Chỉ trả về duy nhất một mảng JSON chứa các string ID, không giải thích, không markdown.
      Ví dụ: ["id1", "id2", ...]
    `;

    const result = await model.generateContent(prompt);
    const textResponse = result.response.text();

    try {
      const cleanJson = textResponse
        .replace(/```json/g, "")
        .replace(/```/g, "")
        .trim();
      return JSON.parse(cleanJson);
    } catch (e) {
      return miniCandidates.slice(0, 10).map((c) => c.id);
    }
  }

  private static async getDetailedLocations(
    ids: string[],
    lat: number,
    lng: number,
  ) {
    if (ids.length === 0) return [];
    const query = `
      SELECT 
        l.id, l.name, l.address, l."categoryId", l."priceLevel", l."avgRating", l."ratingCount",
        ST_X(l.location::geometry) as longitude,
        ST_Y(l.location::geometry) as latitude,
        ST_Distance(l.location, ST_SetSRID(ST_MakePoint($2, $3), 4326)::geography) as distance,
        json_build_object('nameVi', c."nameVi", 'icon', c.icon) as category,
        COALESCE((SELECT json_agg(json_build_object('url', lp.url)) FROM "LocationPhoto" lp WHERE lp."locationId" = l.id), '[]'::json) as "locationPhotos"
      FROM "Location" l
      LEFT JOIN "LocationCategory" c ON l."categoryId" = c.id
      WHERE l.id = ANY($1)
      ORDER BY distance ASC
    `;
    return await prisma.$queryRawUnsafe<any[]>(query, ids, lng, lat);
  }
}
