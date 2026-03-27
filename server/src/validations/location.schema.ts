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
  priceLevel: z.number().int().min(1).max(5).nullable().optional(),
  type: z.string().default("attraction"),
  metadata: z.record(z.string(), z.any()).nullable().optional(),
};

export const LocationUpdateSchema = {
  body: z.object(coreFields),
};

export const LocationAdminUpdateSchema = {
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
