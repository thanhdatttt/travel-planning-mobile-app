import { z } from "zod";

export const FavoriteParamsSchema = {
  params: z.object({
    id: z.uuid("Invalid favorite ID"),
  }),
};

export const FavoriteCreateSchema = {
  body: z.object({
    itineraryId: z.uuid("Invalid itinerary ID"),
  }),
};

export const FavoriteQuerySchema = {
  query: z.object({
    page: z.coerce.number().int().min(1).default(1),
    limit: z.coerce.number().int().min(1).max(100).default(10),
  }),
};
