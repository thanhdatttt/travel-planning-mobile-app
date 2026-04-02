import express from "express";
import { validate } from "../../middlewares/validate.middleware";
import {
  LocationCreateSchema,
  LocationUpdateSchema,
  LocationAdminUpdateSchema,
  LocationParamsSchema,
  MapLocationQuerySchema,
  LocationSearchSchema,
} from "../../validations/location.schema";
import { locationController } from "../../controllers/location.controller";

const router = express.Router();
router.get(
  "/search",
  validate(LocationSearchSchema),
  locationController.search,
);
router.get("/near-by", locationController.getMapLocations);
router.get("/:id", validate(LocationParamsSchema), locationController.getById);

export default router;
