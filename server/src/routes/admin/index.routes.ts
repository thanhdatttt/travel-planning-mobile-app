import express from "express";

import userManageRoute from "./user_manage.routes";
import locationManageRoute from "./location_manage.routes";

const router = express.Router();

router.use("/user", userManageRoute)
router.use("/location", locationManageRoute)

export default router;