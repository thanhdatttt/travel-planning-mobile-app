import express from "express";
import * as itineraryController from "../../controllers/itinerary/itinerary.controller";
import * as itineraryItemController from "../../controllers/itinerary/itineraryItem.controller";

const router = express.Router();

// itinerary
router.post("/", itineraryController.createItinerary);
router.get("/:id", itineraryController.getItinerary);
router.get("/", itineraryController.getUserItineraries);
router.delete("/:id", itineraryController.deleteItinerary);

router.patch("/:id/dates", itineraryController.updateDates);
router.patch("/:id/privacy", itineraryController.changePrivacy);

// itinerary item
router.post("/:id/item", itineraryItemController.addItineraryItem);
router.patch("/:id/item/:itemId/schedule", itineraryItemController.scheduleItineraryItem);
router.delete("/:id/item/:itemId", itineraryItemController.deleteItineraryItem);
export default router;