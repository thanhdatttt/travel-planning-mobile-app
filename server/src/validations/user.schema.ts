import { z } from "zod";

export const updateUserSchema = {
  body: z.object({
    username: z
      .string()
      .min(3, "Username must be at least 3 characters")
      .max(20)
      .regex(
        /^[a-zA-Z0-9_]+$/,
        "Username can only contain letters, numbers, and underscores",
      )
      .optional(),

    email: z.email({ message: "Invalid email format" }).optional(),
    fullName: z
      .string()
      .min(1, "Name cannot be empty")
      .max(50)
      .optional()
      .nullable(),
    phone: z
      .string()
      .regex(/^\+?[1-9]\d{1,14}$/, "Invalid phone number")
      .optional()
      .nullable(),
    address: z.string().max(255).optional().nullable(),
    avatarUrl: z.url({ message: "Invalid image URL" }).optional().nullable(),
    bio: z
      .string()
      .max(500, "Bio must be under 500 characters")
      .optional()
      .nullable(),
    preference: z
      .object({
        theme: z.enum(["light", "dark"]).optional(),
        notifications: z.boolean().optional(),
      })
      .optional(),
  }),
};
