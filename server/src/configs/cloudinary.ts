import { v2 as cloudinary } from "cloudinary";
import { CloudinaryStorage } from "multer-storage-cloudinary";
import multer from "multer";
import { config } from "./config";

const cloudName = process.env.CLOUDINARY_NAME;
const apiKey = process.env.CLOUDINARY_KEY;
const apiSecret = process.env.CLOUDINARY_SECRET;

if (!cloudName || !apiKey || !apiSecret) {
  throw new Error("Cloudinary config is missing in .env file");
}

cloudinary.config({
  cloud_name: cloudName,
  api_key: apiKey,
  api_secret: apiSecret,
});

const storage = new CloudinaryStorage({
  cloudinary: cloudinary,
  params: async (req, file) => {
    return {
      folder: "travel_app_avatars",
      allowed_formats: ["jpg", "png", "jpeg"],
      public_id: `avatar-${Date.now()}-${file.originalname.split(".")[0]}`,
    };
  },
});

export const uploadCloud = multer({
  storage: storage,
  limits: { fileSize: 5 * 1024 * 1024 }, // Giới hạn 5MB
});
