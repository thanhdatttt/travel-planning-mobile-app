import z from "zod"

export const userListQuerySchema = {
    query: z.object({
        usernameOrEmail: z.string().default(""),
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
        minRating: z.coerce.number().min(0).default(0),
        maxRating: z.coerce.number().min(0).max(10).default(10),
        sortBy: z.enum(["name", "priceLevel", "distance", "avgRating"]).default("name"),
        sortOrder: z.enum(["asc", "desc"]).default("asc"),
        type: z.string().default("")
    }),
}

export const updateProfileSchema ={ 
    params: z.object({
        id: z.string()
    }),
    body: z.object({
        fullName: z.string().min(1, "Name cannot be empty").optional(),
        email: z.string().email("Invalid email format").optional(),
        phone: z.string().optional(),
        address: z.string().optional(),
        dob: z.coerce.date().optional(),
        role: z.enum(["user", "admin", "moderator"]).optional(),
        }),
}

export const statQuerySchema = {
    query: z.object({
        month: z.coerce.number().min(1).max(12).optional(),
        year: z.coerce.number().optional(),
    }),
};