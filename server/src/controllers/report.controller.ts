// src/controllers/report.controller.ts
import { Request, Response } from "express";
import { createResponse } from "../utils/response";
import { prisma } from "../libs/prisma";
import { objType } from "../generated/prisma/enums";

export const reportController = {
  async create(req: Request, res: Response) {
    const reporterId = req.user.id;
    
    const { targetType, targetId, reason } = req.body;

    const report = await prisma.report.create({
      data: {
        reporterId,
        targetType: targetType as objType, 
        targetId,
        reason,
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