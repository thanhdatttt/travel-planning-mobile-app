import express from "express";
import itineraryRoutes from "./itinerary.routes";

const router = express.Router();

router.use("/itinerary", itineraryRoutes);

export default router;