import { z } from "zod";

const ObjTypeEnum = z.enum(["location", "review", "itinerary"]);
const ReportStatusEnum = z.enum(["pending", "processed"]);

export const ReportParamsSchema = {
  params: z.object({
    id: z.string().uuid("Invalid report ID"),
  }),
};

export const ReportCreateSchema = {
  body: z.object({
    targetType: ObjTypeEnum,
    targetId: z.uuid("Invalid target ID"),
    reason: z
      .string()
      .min(10, "Reason must be at least 10 characters long")
      .max(500),
  }),
};

export const ReportQuerySchema = {
  query: z.object({
    status: ReportStatusEnum.optional(),
    targetType: ObjTypeEnum.optional(),
    page: z.coerce.number().int().min(1).default(1),
    limit: z.coerce.number().int().min(1).max(100).default(10),
  }),
};
