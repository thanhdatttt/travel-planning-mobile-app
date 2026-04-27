import express from "express";

import * as locationManageController from "../../controllers/admin/location_manage.controller";
import { validate } from "../../middlewares/validate.middleware";
import * as adminSchema from "../../validations/admin.schema";
import { uploadCloud } from "../../configs/cloudinary";
const router = express.Router();

router.get("/list", validate(adminSchema.locationListQuerySchema), locationManageController.getList);
router.patch("/:id", validate(adminSchema.updateLocationSchema), locationManageController.updateLocation);
router.patch("/soft-delete/:id", validate(adminSchema.toggleSoftDeleteSchema), locationManageController.toggleSoftDelete);
router.post("/create", validate(adminSchema.createLocationSchema), locationManageController.createLocation);
router.post("/upload-photos", uploadCloud.array("photos", 5), locationManageController.uploadLocationPhotos);
export default router;
