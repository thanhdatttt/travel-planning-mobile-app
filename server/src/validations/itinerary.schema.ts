import z from "zod";
import { id } from "zod/locales";

// itinerary
export const createItinerarySchema = {
  body: z
    .object({
      title: z
        .string()
        .min(1, "Title is required")
        .max(255, "Title is too long"),
      startDate: z
        .string()
        .min(1, "Start date is required")
        .regex(/^\d{4}-\d{2}-\d{2}$/, "Invalid start date format"),
      endDate: z
        .string()
        .min(1, "End date is required")
        .regex(/^\d{4}-\d{2}-\d{2}$/, "Invalid end date format"),
    })
    .refine((data) => new Date(data.startDate) <= new Date(data.endDate), {
      message: "End date must be after or equal to start date",
      path: ["endDate"],
    }),
};

export const paginationSchema = {
  query: z.object({
    page: z.coerce.number().min(1).default(1),
    limit: z.coerce.number().min(1).max(100).default(10),
  }),
};

export const idParamSchema = {
  params: z.object({
    id: z.uuid("Invalid itinerary ID format"),
  }),
};

export const updateItinerarySchema = {
  params: z.object({
    id: z.uuid("Invalid itinerary ID format"),
  }),
  body: z.object({
    title: z
      .string()
      .min(1, "Title is required")
      .max(255, "Title is too long"),
    description: z
      .string()
      .max(500, "Description is too long (maximum 500 characters)"),
    privacy: z.enum(["public", "private"]).optional(),
    startDate: z
      .string()
      .min(1, "Start date is required")
      .regex(/^\d{4}-\d{2}-\d{2}$/, "Invalid start date format"),
    endDate: z
      .string()
      .min(1, "End date is required")
      .regex(/^\d{4}-\d{2}-\d{2}$/, "Invalid end date format"),
  })
  .partial()
  .refine((data) => {
    if (data.startDate && data.endDate) {
      return new Date(data.startDate) <= new Date(data.endDate);
    }
    return true;
  }, {
    message: "End date must be after or equal to start date",
    path: ["endDate"],
  }),
}

// itinerary item
export const addItemSchema = {
  params: z.object({
    id: z.uuid("Invalid itinerary ID format"),
  }),
  body: z.object({
    locationId: z.uuid("Invalid location ID format"),
    note: z
      .string()
      .min(1, "Note is required")
      .max(500, "Note is too long (maximum 500 characters)")
      .optional(),
  }),
}

export const idItemParamSchema = {
  params: z.object({
    id: z.uuid("Invalid itinerary ID format"),
    itemId: z.uuid("Invalid itinerary item ID format"),
  }),
}

export const scheduleItemSchema = {
  params: z.object({
    id: z.uuid("Invalid itinerary ID format"),
    itemId: z.uuid("Invalid itinerary item ID format"),
  }),
  body: z.object({
    targetDate: z
      .string()
      .min(1, "Date is required")
      .regex(/^\d{4}-\d{2}-\d{2}$/, "Invalid date format"),
  }),
};

export const updateItemNoteSchema = {
  params: z.object({
    id: z.uuid("Invalid itinerary ID format"),
    itemId: z.uuid("Invalid itinerary item ID format"),
  }),
  body: z.object({
    note: z
      .string()
      .min(1, "Note is required")
      .max(500, "Note is too long (maximum 500 characters)"),
  }),
}