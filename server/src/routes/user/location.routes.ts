import express from "express";
import { validate } from "../../middlewares/validate.middleware";
import {
  LocationCreateSchema,
  LocationUpdateSchema,
  LocationAdminUpdateSchema,
  LocationParamsSchema,
  MapLocationQuerySchema,
} from "../../validations/location.schema";
import { locationController } from "../../controllers/location.controller";

const router = express.Router();

router.get("/near-by", locationController.getMapLocations);
router.get("/:id", validate(LocationParamsSchema), locationController.getById);

export default router;
