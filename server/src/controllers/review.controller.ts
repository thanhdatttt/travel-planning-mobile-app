import { Request, Response } from "express";
import { createResponse } from "../utils/response";
import { prisma } from "../libs/prisma";
import ApiError from "../utils/apiError";

export const reviewController = {
  async create(req: Request, res: Response) {
    const userId = req.user.id;
    const { title, body, rating, locationId } = req.body;

    const existingReview = await prisma.review.findUnique({
      where: { userId_locationId: { userId, locationId } },
    });

    if (existingReview) {
      throw new ApiError(400, "You have already reviewed this location");
    }

    const review = await prisma.review.create({
      data: { title, body, rating, locationId, userId },
    });

    return res.status(201).json(createResponse({ 
      message: "Review created successfully", 
      data: review 
    }));
  },

  async getAll(req: Request, res: Response) {
    const page = Number(req.query.page) || 1;
    const limit = Number(req.query.limit) || 10;
    const skip = (page - 1) * limit;

    const [reviews, total] = await Promise.all([
      prisma.review.findMany({
        where: { isDeleted: false },
        skip,
        take: limit,
        orderBy: { createdAt: "desc" },
        // include: { user: { select: { name: true } } } // include user name (for viewing other users review)
      }),
      prisma.review.count({ where: { isDeleted: false } }),
    ]);

    return res.json(createResponse({
      data: reviews,
      metadata: {
        total,
        page,
        lastPage: Math.ceil(total / limit),
      },
    }));
  },

  async getById(req: Request, res: Response) {
    const { id } = req.params as {id: string};

    const review = await prisma.review.findFirst({
      where: { id, isDeleted: false },
    });

    if (!review) throw new ApiError(404, "Review not found");

    return res.json(createResponse({ data: review }));
  },

  async update(req: Request, res: Response) {
    const { id } = req.params as {id: string};
    const userId = req.user.id;

    const review = await prisma.review.findUnique({ where: { id } });
    if (!review || review.isDeleted) throw new ApiError(404, "Review not found");
    if (review.userId !== userId) throw new ApiError(403, "Not authorized to update this review");

    const updatedReview = await prisma.review.update({
      where: { id },
      data: req.body,
    });

    return res.json(createResponse({ 
      message: "Review updated successfully", 
      data: updatedReview 
    }));
  },

  //Soft Delete
  async delete(req: Request, res: Response) {
    const { id } = req.params as {id: string};
    const userId = req.user.id;

    const review = await prisma.review.findUnique({ where: { id } });
    if (!review) throw new ApiError(404, "Review not found");
    if (review.userId !== userId) throw new ApiError(403, "Not authorized to delete this review");

    await prisma.review.update({
      where: { id },
      data: { 
        isDeleted: true,
        deletedAt: new Date() 
      },
    });

    return res.json(createResponse({ message: "Review deleted successfully" }));
  },
};