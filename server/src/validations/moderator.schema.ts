import z from "zod";

export const getReportQuerySchema = {
    query: z.object({
        skip: z.coerce.number().min(0).default(0),
        take: z.coerce.number().min(1).max(100).default(20),
    }),
};

export const dismissReportSchema = {
    params: z.object({
        id: z.string().uuid(),
    }),
};
