import express from "express";
import { userController } from "../../controllers/user.controller";
import { validate } from "../../middlewares/validate.middleware";
import {
  updateUserSchema,
  uploadAvatarSchema,
} from "../../validations/user.schema";
import { uploadCloud } from "../../configs/cloudinary";

const router = express.Router();

router.get("/me", userController.getMe);
router.post("/", validate(updateUserSchema), userController.updateUser);
router.post(
  "/upload-avatar",
  uploadCloud.single("avatar"),
  validate(uploadAvatarSchema),
  userController.uploadAvatar
);

export default router;
