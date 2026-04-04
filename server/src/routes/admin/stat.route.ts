import express from "express";
import { validate } from "../../middlewares/validate.middleware";
import * as adminSchema from "../../validations/admin.schema";
import * as statController from "../../controllers/admin/stat.controller";

const router = express.Router();

router.get("sign-in-log", statController.getSignInLog);

export default router;