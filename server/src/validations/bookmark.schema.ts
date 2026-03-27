import { z } from "zod";

export const BookmarkParamsSchema = {
  params: z.object({
    id: z.uuid("Invalid bookmark ID"),
  }),
};

export const BookmarkCreateSchema = {
  body: z.object({
    locationId: z.uuid("Invalid location ID"),
  }),
};

export const BookmarkQuerySchema = {
  query: z.object({
    page: z.coerce.number().int().min(1).default(1),
    limit: z.coerce.number().int().min(1).max(100).default(10),
  }),
};