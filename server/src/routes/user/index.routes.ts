import express from "express";
import userRoutes from "./user.routes"
import locationRoutes from "./location.routes";
import reviewRoutes from "./review.routes";
import bookmarkRoutes from "./bookmark.routes";

const router = express.Router();

router.use('/users', userRoutes);
router.use('/locations', locationRoutes);
router.use('/reviews', reviewRoutes);
router.use('/bookmarks', bookmarkRoutes);

export default router;
