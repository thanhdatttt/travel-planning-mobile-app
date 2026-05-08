import axios from "axios";
import { Request, Response } from "express";
import { prisma } from "../../libs/prisma";
import { Prisma } from '../../generated/prisma/client';
import { createResponse } from "../../utils/response";
import { geocodeAddress } from "../../utils/geocoder";

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
  const categoryId = categoryParam ? categoryParam.split(",").map((c) => Number(c.trim())).filter(val => !isNaN(val)) : [1, 2, 3, 4, 5];

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
    l."${Prisma.raw(sortByStr)}" ${Prisma.raw(sortOrderStr)}
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
  console.log(req.body)

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

export const createLocation = async (req: Request, res: Response) => {
  const { 
    name, 
    address, 
    description, 
    website, 
    phone,
    priceLevel, 
    categoryId, 
    imgUrls
  } = req.body;

  const createdBy = req.user?.id || null;

  const coords = await geocodeAddress(address);
    
    if (!coords) {
      return res.status(400).json(
        createResponse({
          message: "Could not find coordinates for this address. Please try a more specific address.",
          data: null
        })
      );
    }

  const newLocation = await prisma.$transaction(async (tx) => {
    
    const result = await tx.$queryRaw`
      INSERT INTO "Location" (
        id, name, address, description, website, phone, "priceLevel", "categoryId", "createdBy", location, "updatedAt"
      ) 
      VALUES (
        gen_random_uuid(), 
        ${name}, 
        ${address}, 
        ${description || null}, 
        ${website || null}, 
        ${phone || null},
        ${priceLevel || null}, 
        ${Number(categoryId)}, 
        ${createdBy}, 
        ST_SetSRID(ST_MakePoint(${coords.lng}, ${coords.lat}), 4326)::geography, 
        NOW()
      ) 
      RETURNING *;
    `;

    const insertedLocation = (result as any[])[0];

    if (imgUrls && Array.isArray(imgUrls) && imgUrls.length > 0) {
      const photoData = imgUrls.map((url, index) => ({
        locationId: insertedLocation.id,
        uploaderId: createdBy,
        url: url,
        isFeature: index === 0
      }));

      await tx.locationPhoto.createMany({
        data: photoData
      });
    }

    return insertedLocation;
  });

  return res.status(201).json(
    createResponse({
      message: "Location created successfully",
      data: newLocation,
    })
  );
};

export const uploadLocationPhotos = async (req: Request, res: Response) => {
  const files = req.files as Express.Multer.File[];
  
  if (!files || files.length === 0) {
    return res.status(400).json(
      createResponse({
        message: "No files uploaded",
        data: []
      })
    );
  }

  const imageUrls = files.map((file: any) => file.path);

  return res.status(200).json(
    createResponse({
      message: "Uploaded location photos successfully",
      data: imageUrls,
    })
  );
};
