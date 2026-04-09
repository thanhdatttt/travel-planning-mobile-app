import { Request, Response } from "express";
import { createResponse } from "../../utils/response";
import { prisma } from "../../libs/prisma";
import ApiError from "../../utils/apiError";

export const reportManageController = {

  async getAll(req: Request, res: Response) {
    const { status, targetType } = req.query;
    const page = Number(req.query.page) || 1;
    const limit = Number(req.query.limit) || 10;
    const skip = (page - 1) * limit;

    const where = {
      ...(status && { status: status as any }),
      ...(targetType && { targetType: targetType as any }),
    };

    const [reports, total] = await Promise.all([
      prisma.report.findMany({
        where,
        skip,
        take: limit,
        orderBy: { createdAt: "desc" },
      }),
      prisma.report.count({ where }),
    ]);

    return res.json(
      createResponse({
        data: reports,
        metadata: { total, page, lastPage: Math.ceil(total / limit) },
      }),
    );
  },

  // Admin function to process a report
  async process(req: Request, res: Response) {
    const { id } = req.params as { id: string };
    const modId = req.user.id; // The ID of the mod handling it

    const report = await prisma.report.findUnique({ where: { id } });
    if (!report) throw new ApiError(404, "Report not found");

    const updatedReport = await prisma.report.update({
      where: { id },
      data: {
        status: "processed",
        handledBy: modId,
        handledAt: new Date(),
      },
    });

    return res.json(
      createResponse({
        message: "Report processed",
        data: updatedReport,
      }),
    );
  },
};
