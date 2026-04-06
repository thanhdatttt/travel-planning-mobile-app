import express from "express";

import userManageRoute from "./user_manage.routes";
import locationManageRoute from "./location_manage.routes";
import statRoute from "./stat.route";
import reportRoute from "./report.routes";
import { requireRole } from "../../middlewares/auth.middleware";

const router = express.Router();

router.use(requireRole("admin"));
router.use("/user", userManageRoute);
router.use("/location", locationManageRoute);
router.use("/stat", statRoute);
router.use("/reports", reportRoute);

export default router;
