import express from "express";
import { validate } from "../../middlewares/validate.middleware";
import {
  ReportQuerySchema,
  ReportCreateSchema,
  ReportParamsSchema,
} from "../../validations/report.schema";
import { reportController } from "../../controllers/report.controller";

const router = express.Router();

router.post("/", validate(ReportCreateSchema), reportController.create);

export default router;