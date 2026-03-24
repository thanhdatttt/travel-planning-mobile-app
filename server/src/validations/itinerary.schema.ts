import z from "zod";

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
        .refine((val) => !isNaN(Date.parse(val)), "Invalid start date format"),
      endDate: z
        .string()
        .min(1, "End date is required")
        .refine((val) => !isNaN(Date.parse(val)), "Invalid end date format"),
    })
    .refine((data) => new Date(data.startDate) <= new Date(data.endDate), {
      message: "End date must be after or equal to start date",
      path: ["endDate"],
    }),
};

export const itineraryIdParamSchema = {
  params: z.object({
    id: z.uuid("Invalid itinerary ID format"),
  }),
};

export const updateDatesSchema = {
  params: z.object({
    id: z.uuid("Invalid itinerary ID format"),
  }),
  body: z
    .object({
      startDate: z
        .string()
        .min(1, "Start date is required")
        .refine((val) => !isNaN(Date.parse(val)), "Invalid start date format"),
      endDate: z
        .string()
        .min(1, "End date is required")
        .refine((val) => !isNaN(Date.parse(val)), "Invalid end date format"),
    })
    .refine((data) => new Date(data.startDate) <= new Date(data.endDate), {
      message: "End date must be after or equal to start date",
      path: ["endDate"],
    }),
};

export const changePrivacySchema = {
  params: z.object({
    id: z.uuid("Invalid itinerary ID format"),
  }),
  body: z.object({
    privacy: z.enum(["public", "private"]),
  }),
};

// itinerary item
export const addItemSchema = {
  params: z.object({
    itemId: z.uuid("Invalid itinerary ID format"),
  }),
  body: z.object({
    locationId: z.string().uuid("Invalid location ID format"),
    note: z
      .string()
      .max(500, "Note is too long (maximum 500 characters)")
      .optional(),
  }),
}

export const deleteItemSchema = {
  params: z.object({
    itemId: z.uuid("Invalid itinerary ID format"),
  }),
}

export const scheduleItemSchema = {
  params: z.object({
    itemId: z.uuid("Invalid itinerary item ID format"),
  }),
  body: z.object({
    targetDayNumber: z
      .number({
        error: "Target day number must be a number",
      })
      .int("Day number must be an integer")
      .min(1, "Day number must be at least 1"),
  }),
};
