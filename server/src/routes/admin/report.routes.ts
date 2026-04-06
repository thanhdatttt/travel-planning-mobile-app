import express from "express";
import { validate } from "../../middlewares/validate.middleware";
import {
  ReportQuerySchema,
  ReportCreateSchema,
  ReportParamsSchema,
} from "../../validations/report.schema";
import { reportManageController } from "../../controllers/admin/report_manage.controller";

const router = express.Router();

router.get("/", validate(ReportQuerySchema), reportManageController.getAll);
router.patch("/:id/process", validate(ReportParamsSchema), reportManageController.process);

export default router;