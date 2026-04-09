import { Request, Response } from "express";
import { RecommendationUtils } from "../utils/recommendationUtils";

export const recommendationController = {
  recommendLocations: async (req: Request, res: Response) => {
    const userId = req.user?.id;
    const { lat, lng } = req.body;

    if (!userId || !lat || !lng) {
      return res.status(400).json({
        success: false,
        message: "Missing required parameters: userId, lat, lng",
      });
    }

    const finalRecommendations =
      await RecommendationUtils.getSmartRecommendations(
        userId,
        Number(lat),
        Number(lng),
      );

    return res.status(200).json({
      success: true,
      message: "Get recommendations successfully.",
      data: finalRecommendations,
    });
  },
};
