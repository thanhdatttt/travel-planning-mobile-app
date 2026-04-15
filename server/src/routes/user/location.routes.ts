import express from "express";
import { validate } from "../../middlewares/validate.middleware";
import {
  LocationCreateSchema,
  LocationUpdateSchema,
  LocationAdminUpdateSchema,
  LocationParamsSchema,
  MapLocationQuerySchema,
  LocationSearchSchema,
  uploadLocationPhotoSchema,
} from "../../validations/location.schema";
import { locationController } from "../../controllers/location.controller";
import { locationPhotoController } from "../../controllers/locationphoto.controller";
import { uploadCloud } from "../../configs/cloudinary";

const router = express.Router();
router.get(
  "/search",
  validate(LocationSearchSchema),
  locationController.search,
);
router.get("/near-by", locationController.getMapLocations);
router.get("/:id", validate(LocationParamsSchema), locationController.getById);

router.post(
  "/:locationId/photos",
  uploadCloud.single("photo"),
  validate(uploadLocationPhotoSchema),
  locationPhotoController.uploadPhoto,
);

export default router;
