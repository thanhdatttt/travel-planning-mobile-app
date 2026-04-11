import { Request, Response } from "express";
import { createResponse } from "../utils/response";
import { prisma } from "../libs/prisma";
import ApiError from "../utils/apiError";

export const reportController = {
  async create(req: Request, res: Response) {
    const reporterId = req.user.id;
    const { targetType, targetId, reason } = req.body;

    const existingReport = await prisma.report.findUnique({
      where: {
        reporterId_targetId: { reporterId, targetId },
      },
    });

    if (existingReport) {
      throw new ApiError(400, "You have already reported this item");
    }

    const report = await prisma.report.create({
      data: {
        reporterId,
        targetType,
        targetId,
        reason,
        status: "pending",
      },
    });

    return res.status(201).json(
      createResponse({
        message: "Report submitted successfully",
        data: report,
      }),
    );
  },
};
