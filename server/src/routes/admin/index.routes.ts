import express from "express";

import userManageRoute from "./user_manage.routes";
import locationManageRoute from "./location_manage.routes";
import { requireRole } from "../../middlewares/auth.middleware";

const router = express.Router();

router.use(requireRole("admin"));
router.use("/user", userManageRoute);
router.use("/location", locationManageRoute);

export default router;
