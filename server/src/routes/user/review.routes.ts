import express from "express";
import { validate } from "../../middlewares/validate.middleware";
import {
  ReviewQuerySchema,
  ReviewCreateSchema,
  ReviewParamsSchema,
  ReviewUpdateSchema,
} from "../../validations/review.schema";
import { reviewController } from "../../controllers/review.controller";

const router = express.Router();

router.get("/:id", validate(ReviewParamsSchema), reviewController.getById);
router.get("/", validate(ReviewQuerySchema), reviewController.getAllByLocationId);
router.post("/", validate(ReviewCreateSchema), reviewController.create);
router.patch("/", validate(ReviewUpdateSchema), reviewController.update);

router.get("/stats/:locationId", reviewController.getStats);

export default router;
