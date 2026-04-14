import express from "express";
import userRoutes from "./user.routes";
import locationRoutes from "./location.routes";
import reviewRoutes from "./review.routes";
import bookmarkRoutes from "./bookmark.routes";
import categoryRoutes from "./category.routes";
import recommendationRoutes from "./recommendation.routes";
import reportRoutes from "./report.routes";
const router = express.Router();

router.use("/users", userRoutes);
router.use("/locations", locationRoutes);
router.use("/reviews", reviewRoutes);
router.use("/bookmarks", bookmarkRoutes);
router.use("/categories", categoryRoutes);
router.use("/recommendation", recommendationRoutes);
router.use("/reports", reportRoutes);
export default router;
