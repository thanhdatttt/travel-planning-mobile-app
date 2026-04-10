import express from "express";
import { validate } from "../../middlewares/validate.middleware";
import z from "zod";

import * as userManageController from "../../controllers/admin/user_manage.controller"
import * as adminSchema from "../../validations/admin.schema";

const router = express.Router();

router.get("/list", validate(adminSchema.userListQuerySchema) , userManageController.getList);
router.post("/ban/:id", validate(adminSchema.toggleBanSchema) , userManageController.toggleBan);
router.delete("/soft-delete/:id", validate(adminSchema.toggleSoftDeleteSchema) , userManageController.toggleSoftDeleteUser);
router.post("/update-password/:id", validate(adminSchema.updatePasswordSchema), userManageController.updatePassword);
router.post("/demote-moderator/:id", validate(adminSchema.updateRoleSchema), userManageController.demoteFromModerator);

export default router;