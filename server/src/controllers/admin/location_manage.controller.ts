import axios from "axios";
import { Request, Response } from "express";
import { prisma } from "../../libs/prisma";
import { config } from "../../configs/config";
import { createResponse } from "../../utils/response";
import { locationType, userRole } from "../../generated/prisma/browser";
import { equal } from "node:assert";

export const getList = async (req: Request, res: Response) => {
    const {name = "", sortBy = "name", sortOrder = "asc", minPrice, maxPrice, 
        minDistance, maxDistance, minRating, maxRating} = req.query;
    const type = req.query.type as string;

    const locations = await prisma.location.findMany({
        where: {
            AND: [
                { name: { contains: String(name), mode: 'insensitive' } },
                { priceLevel: {gte: Number(minPrice)}},
                { priceLevel: {lte: Number(maxPrice)}},
                { avgRating: {gte: Number(minRating)}},
                { avgRating: {lte: Number(maxRating)}},
                { type: type?.toLowerCase() === "attraction" ? locationType.attraction : locationType.restaurant}
            ]}
        }),
}