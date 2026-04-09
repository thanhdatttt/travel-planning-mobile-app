import z from "zod"

export const userListQuerySchema = {
    query: z.object({
        username: z.string().default(""),
        email: z.string().default(""),
        role: z.string().default(""), 
        isBanned: z.preprocess((val) => val === 'true', z.boolean()).default(false),
        isInactive: z.preprocess((val) => val === 'true', z.boolean()).default(false),
        sortBy: z.enum(["username", "email", "createdAt"]).default("username"),
        sortOrder: z.enum(["asc", "desc"]).default("asc"),
    }),
};

export const toggleBanSchema = {
    params: z.object({
        id: z.string()
    }),
    body: z.object({
        ban: z.boolean()
    })
}

export const toggleSoftDeleteSchema = {
    params: z.object({
        id: z.string()
    }),
    body: z.object({
        delete: z.boolean()
    })
}

export const updateRoleSchema = {
    params: z.object({
        id: z.string()
    })
}

export const updatePasswordSchema = {
    params: z.object({
        id: z.string()
    }),
    body: z.object({
        newPassword: z.string().min(6, "Password must be at least 6 characters long")
    })
}

export const locationListQuerySchema = {
    query: z.object({
        name: z.string().default(""),
        minPrice: z.coerce.number().min(0).default(0),
        maxPrice: z.coerce.number().min(0).max(Number.MAX_VALUE).default(Number.MAX_VALUE),
        minDistance: z.coerce.number().min(0).default(0),
        maxDistance: z.coerce.number().min(0).max(Number.MAX_VALUE).default(Number.MAX_VALUE),
        minRating: z.coerce.number().min(0).default(0),
        maxRating: z.coerce.number().min(0).max(10).default(10),
        sortBy: z.enum(["name", "price", "distance", "rating"]).default("name"),
        sortOrder: z.enum(["asc", "desc"]).default("asc"),
        type: z.string().default("")
    }),
}