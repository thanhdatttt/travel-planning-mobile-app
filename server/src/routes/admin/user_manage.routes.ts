import express from "express";
import { validate } from "../../middlewares/validate.middleware";

import * as userManageController from "../../controllers/admin/user_manage.controller"

import * as adminSchema from "../../validations/admin.schema";

const router = express.Router();

router.get("/list", validate(adminSchema.userListQuerySchema) , userManageController.getList);
router.post("/:id")

export default router;