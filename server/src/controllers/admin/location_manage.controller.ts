import axios from "axios";
import { Request, Response } from "express";
import { prisma } from "../../libs/prisma";
import { Prisma } from '../../generated/prisma/client';
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

  const searchName = `%${name}%`;
  const categoryIds = Array.isArray(categoryId) ? categoryId : [categoryId];
  const sortByStr = String(sortBy || "name");
  const sortOrderStr = String(sortOrder || "asc").toUpperCase();
  const isDeleted = req.query.isDeleted === 'true';

  const locations = await prisma.$queryRaw`
  SELECT 
    l.id, 
    l.name, 
    l."avgRating", 
    l."priceLevel",
    l."address",
    l."isDeleted",
    l."categoryId",
    (
      SELECT json_agg(json_build_object(
        'url', lp.url,
        'id', lp.id,
        'isFeature', lp."isFeature"
      ))
      FROM "LocationPhoto" lp 
      WHERE lp."locationId" = l.id 
    ) AS "photos"
    FROM "Location" l
  WHERE 
    l.name ILIKE ${searchName}
    AND l."priceLevel" >= ${Number(minPrice)}
    AND l."priceLevel" <= ${Number(maxPrice)}
    AND l."avgRating" >= ${Number(minRating)}
    AND l."avgRating" <= ${Number(maxRating)}
    AND l."categoryId" IN (${Prisma.join(categoryIds)})
    AND l."isDeleted" = ${isDeleted}
  ORDER BY 
    l.${Prisma.raw(sortByStr)} ${Prisma.raw(sortOrderStr)}
  LIMIT ${Number(take)} 
  OFFSET ${Number(skip)}
`;

  return res.status(200).json(
    createResponse({
      message: "Locations retrieved successfully",
      data: locations,
    }),
  );
};

export const updateLocation = async (req: Request, res: Response) => {
  const { id } = req.params;
  const { name, address, priceLevel, phone, avgRating, categoryId, imgUrl } = req.body;

  const updateData: any = {};
  if (name) updateData.name = name;
  if (address) updateData.address = address;
  if (priceLevel) updateData.priceLevel = priceLevel;
  if (phone) updateData.phone = phone;
  if (avgRating) updateData.avgRating = avgRating;
  if (categoryId) updateData.categoryId = categoryId;
  if (imgUrl) updateData.imgUrl = imgUrl;

  const location = await prisma.location.update({
    where: { id: String(id) },
    data: updateData,
  });

  return res.status(200).json(
    createResponse({
      message: "Location updated successfully",
      data: location,
    }),
  );

}
export const toggleSoftDelete = async (req: Request, res: Response) => {
  const { id } = req.params;
  const { delete: isDeleted } = req.body;

  const updatedLocation = await prisma.location.update({
        where: { id: String(id)},
        data: {isDeleted: Boolean(isDeleted)}
    });

    return res.status(200).json(
        createResponse({
            message: isDeleted ? "Location soft-deleted successfully" : "Location un-soft-deleted successfully",
            data: updatedLocation
        })
    );
}
