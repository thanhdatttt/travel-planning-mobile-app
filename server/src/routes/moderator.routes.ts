import express from "express";
import { moderatorController } from "../controllers/moderator.comtroller";
import { validate } from "../middlewares/validate.middleware";
import * as schema  from "../validations/moderator.schema";
import { toggleBanSchema } from "../validations/admin.schema";

const router = express.Router();
    
router.get("/reports/review", validate(schema.getReportQuerySchema), moderatorController.getReportsReview);
router.get("/reports/location", validate(schema.getReportQuerySchema), moderatorController.getReportsLocation);
router.get("/reports/itinerary", validate(schema.getReportQuerySchema), moderatorController.getReportsItinerary);
router.post("/ban-user/:id", validate(toggleBanSchema), moderatorController.banUser);
router.post("/dismiss/:id", validate(schema.dismissReportSchema), moderatorController.dismissReport);

export default router;