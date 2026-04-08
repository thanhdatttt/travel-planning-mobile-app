import { prisma } from "../libs/prisma";
import { GoogleGenerativeAI } from "@google/generative-ai";

const genAI = new GoogleGenerativeAI(process.env.GEMINI_API_KEY || "");
const model = genAI.getGenerativeModel({
  model: "gemini-2.5-pro",
});

export class RecommendationService {
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

      const aiResults = await this.getLayer3AILogic(candidatePool);
      const aiIds = aiResults.map((item: any) => item.id);

      const detailedLocations = await this.getDetailedLocations(
        aiIds,
        lat,
        lng,
      );

      return detailedLocations.map((loc) => {
        const aiInfo = aiResults.find((ai: any) => ai.id === loc.id);
        return {
          ...loc,
          reason:
            aiInfo?.reason ||
            "Place is recommended based on your preferences and current location.",
        };
      });
    } catch (error) {
      console.error("Error calling AI Recommendation:", error);
      const fallbackIds = (
        await this.getLayer1NearestCandidates(lat, lng, 10)
      ).map((l) => l.id);
      const detailedFallback = await this.getDetailedLocations(
        fallbackIds,
        lat,
        lng,
      );

      return detailedFallback.map((loc) => ({
        ...loc,
        reason:
          "Place is recommended based on proximity to your current location.",
      }));
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
        SELECT l."categoryId"
        FROM "Bookmark" b
        JOIN "Location" l ON b."locationId" = l.id
        WHERE b."userId" = $1
        
        UNION ALL
        
        SELECT l."categoryId"
        FROM "Favorite" f
        JOIN "ItineraryItem" ii ON f."itineraryId" = ii."itineraryId"
        JOIN "Location" l ON ii."locationId" = l.id
        WHERE f."userId" = $1
      )
      SELECT "categoryId", COUNT(*) as count
      FROM UserLocations
      GROUP BY "categoryId"
      ORDER BY count DESC
      LIMIT 3;
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

  private static async getLayer3AILogic(candidatePool: any[]) {
    const miniCandidates = candidatePool.map((c) => ({
      id: c.id,
      name: c.name,
      rating: c.avgRating,
    }));

    const prompt = `
      Bạn là chuyên gia du lịch thông minh. 
      Dưới đây là danh sách các địa điểm có sẵn trong database:
      ${JSON.stringify(miniCandidates)}

      Nhiệm vụ:
      1. Chọn ra ĐÚNG 10 địa điểm nổi bật và CÓ SỰ ĐA DẠNG về trải nghiệm từ danh sách trên.
      2. Viết 1 lý do ngắn gọn giải thích tại sao đề xuất địa điểm đó.
      3. TUYỆT ĐỐI KHÔNG bịa thêm địa điểm. Chỉ dùng id có trong danh sách.
      
      Trả về kết quả chuẩn JSON Array, KHÔNG dùng markdown:
      [
        { "id": "uuid", "name": "tên", "reason": "lý do..." }
      ]
    `;

    const result = await model.generateContent(prompt);
    const textResponse = result.response.text();

    const cleanJson = textResponse
      .replace(/```json/g, "")
      .replace(/```/g, "")
      .trim();
    return JSON.parse(cleanJson);
  }
  private static async getDetailedLocations(
    ids: string[],
    lat: number,
    lng: number,
  ) {
    if (ids.length === 0) return [];
    const query = `
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
        ST_SetSRID(ST_MakePoint($2, $3), 4326)::geography
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
      ) as "locationPhotos"
    FROM "Location" l
    LEFT JOIN "LocationCategory" c ON l."categoryId" = c.id
    WHERE l.id = ANY($1)
    ORDER BY distance ASC
  `;

    return await prisma.$queryRawUnsafe<any[]>(query, ids, lng, lat);
  }
}
