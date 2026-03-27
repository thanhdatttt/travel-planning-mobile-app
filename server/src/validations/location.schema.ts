import { z } from "zod";

export const LocationParamsSchema = {
  params: z.object({
    id: z.uuid("Invalid id"),
  }),
};

const coreFields = {
  name: z.string().min(1, "Name is required").max(255),
  description: z.string().nullable().optional(),
  address: z.string().nullable().optional(),
  phone: z.string().nullable().optional(),
  website: z.url("Invalid URL").nullable().optional(),
  priceLevel: z.number().int().min(1).max(4).nullable().optional(),
  metadata: z.record(z.string(), z.any()).nullable().optional(),
  latitude: z.number().min(-90).max(90, "Invalid latitude"),
  longitude: z.number().min(-180).max(180, "Invalid longitude"),
};

export const LocationUpdateSchema = {
  body: z.object(coreFields),
};

export const LocationAdminUpdateSchema = {
  params: z.object({
    id: z.uuid("Invalid id"),
  }),
  body: z.object({
    ...coreFields,
    osmId: z.string().nullable().optional(),
    isDeleted: z.boolean().optional(),
    createdBy: z.uuid().optional(),
    slug: z.string().optional(),
  }),
};

export const LocationCreateSchema = {
  body: z.object({
    ...coreFields,
  }),
};

export const MapLocationQuerySchema = {
  query: z.object({
    lat: z.string().transform(Number),
    lng: z.string().transform(Number),
    radius: z.string().optional().transform(Number).default(5000), // Bán kính 5km
    categoryId: z.string().optional().transform(Number),
  }),
};
