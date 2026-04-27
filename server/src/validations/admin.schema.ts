import z from "zod"
import { ca, is } from "zod/locales";

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
        categoryId: z.coerce.number().optional(),
        isDeleted: z.preprocess((val) => val === 'true', z.boolean()).default(false),
        skip: z.coerce.number().min(0).default(0),
        take: z.coerce.number().min(1).max(100).default(20),
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

export const updateLocationSchema = {
  params: z.object({
    id: z.uuid("Invalid id"),
  }),
  body: z.object({
    name: z.string().optional(),
    address: z.string().optional(),
    priceLevel: z.coerce.number().min(1).max(4).optional(),
    phone: z.string().optional(),
    avgRating: z.coerce.number().min(0).max(5).optional(),
    categoryId: z.coerce.number().optional(),
    imgUrls: z.string().optional(),
  }),
};

export const createLocationSchema = {
  body: z.object({
    name: z.string().min(1, "Name is required"),
    address: z.string().min(1, "Address is required"),
    description: z.string().optional(),
    website: z.string().url("Invalid website URL").optional().or(z.literal('')),
    phone: z.string().optional(),
    priceLevel: z.coerce.number().min(1).max(4).optional(),
    categoryId: z.coerce.number().min(1, "Category ID is required"),
    imgUrls: z.array(z.string()).optional()
  }),
};