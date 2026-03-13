import express from "express";
import { getMe, updateUser } from "../../controllers/user.controller";
import { validate } from "../../middlewares/validate.middleware";
import { updateUserSchema } from "../../validations/user.schema";

const router = express.Router();

router.get("/me", getMe);
router.post("/", validate(updateUserSchema), updateUser);

export default router;
