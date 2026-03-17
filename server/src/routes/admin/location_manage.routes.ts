import express from "express";

import * as locationManageController from "../../controllers/admin/location_manage.controller";
import { validate } from "../../middlewares/validate.middleware";
import * as adminSchema from "../../validations/admin.schema";

const router = express.Router();

router.get("/list", validate(adminSchema.locationListQuerySchema), locationManageController.getList);

export default router;