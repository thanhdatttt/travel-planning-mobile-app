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