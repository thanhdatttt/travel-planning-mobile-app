import { z } from "zod";

export const ReviewParamsSchema = {
  params: z.object({
    id: z.uuid("Invalid review ID"),
  }),
};

const coreFields = {
  title: z.string().min(1, "Title is required").max(255),
  body: z.string().min(1, "Review content is required"),
  rating: z.number().int().min(1).max(5),
  locationId: z.uuid("Invalid location ID"),
};

export const ReviewCreateSchema = {
  body: z.object(coreFields),
};

export const ReviewUpdateSchema = {
  body: z.object({
    title: coreFields.title.optional(),
    body: coreFields.body.optional(),
    rating: coreFields.rating.optional(),
  }),
};

export const ReviewQuerySchema = {
  query: z.object({
    locationId: z.uuid("Invalid locationId ID").optional(),
    page: z.coerce
      .number()
      .int("Page must be an integer")
      .min(1, "Page must be at least 1")
      .default(1),

    limit: z.coerce
      .number()
      .int()
      .min(1)
      .max(100, "Cannot fetch more than 100 items")
      .default(10),
  }),
};
