import axios from "axios";
import { Request, Response } from "express";
import { prisma } from "../../libs/prisma";
import { config } from "../../configs/config";
import { createResponse } from "../../utils/response";
import { userRole } from "../../generated/prisma/browser";
import { equal } from "node:assert";

const MAX_VALUE = 32767;

export const getList = async (req: Request, res: Response) => {
  const {
    name = "",
    sortBy = "name",
    sortOrder = "asc",
    minPrice = 0,
    maxPrice = MAX_VALUE,
    minRating = 0,
    maxRating = 5,
    skip = 0, 
    take = 20
  } = req.query;
  const categoryParam = req.query.categoryId as string;
  const categoryId = categoryParam ? categoryParam.split(",").map((c) => Number(c.trim())).filter(val => !isNaN(val)) : [1, 2, 3, 4];

  const locations = await prisma.location.findMany({
    where: {
      AND: [
        { name: { contains: String(name), mode: "insensitive" } },
        { priceLevel: { gte: Number(minPrice) } },
        { priceLevel: { lte: Number(maxPrice) } },
        { avgRating: { gte: Number(minRating) } },
        { avgRating: { lte: Number(maxRating) } },
        { categoryId: { in: categoryId}}
      ],
    },
    orderBy: {
      [String(sortBy)]: sortOrder,
    },
    include: {
      locationPhotos: true,
    },
    skip: Number(skip),
    take: Number(take)
  });

  console.log(locations[0]);

  return res.status(200).json(
    createResponse({
      message: "Locations retrieved successfully",
      data: locations,
    }),
  );
};
