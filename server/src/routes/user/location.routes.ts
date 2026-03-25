import express from "express";
import { validate } from "../../middlewares/validate.middleware";
import {
  LocationCreateSchema,
  LocationUpdateSchema,
  LocationAdminUpdateSchema,
  LocationParamsSchema,
} from "../../validations/location.schema";
import { locationController } from "../../controllers/location.controller";

const router = express.Router();

router.get("/:id", validate(LocationParamsSchema), locationController.getById);

export default router;
