import express from "express";
import { recommendationController } from "../../controllers/recommendation.controller";

const router = express.Router();
router.post("/smart", recommendationController.recommendLocations);
export default router;
