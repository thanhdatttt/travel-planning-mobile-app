import express from "express";
import userRoutes from "./user.routes"
import locationRoutes from "./location.routes";
import reviewRoutes from "./review.routes";

const router = express.Router();

router.use('/user', userRoutes);
router.use('/location', locationRoutes);
router.use('/review', reviewRoutes);

export default router;
