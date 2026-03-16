import z from "zod"

export const userListQuerySchema = {
    query: z.object({
        name: z.string().default(""),
        email: z.string().default(""),
        role: z.string().default(""), 
        isBanned: z.preprocess((val) => val === 'true', z.boolean()).default(false),
        sortBy: z.enum(["name", "email", "createdAt"]).default("name"),
        sortOrder: z.enum(["asc", "desc"]).default("asc"),
    }),
};