import { validate } from "../../middlewares/validate.middleware";
import express from "express";
import * as itiController from "../../controllers/itinerary/itinerary.controller";
import * as itiItemController from "../../controllers/itinerary/itineraryItem.controller";
import * as itiSchema from "../../validations/itinerary.schema";

const router = express.Router();

// itinerary
router.post("/", 
    validate(itiSchema.createItinerarySchema), itiController.createItinerary);
router.get("/:id",
    validate(itiSchema.itineraryIdParamSchema), itiController.getItinerary);
router.get("/", itiController.getUserItineraries);
router.delete("/:id", 
    validate(itiSchema.itineraryIdParamSchema), itiController.deleteItinerary);
router.patch("/:id/dates",
    validate(itiSchema.updateDatesSchema), itiController.updateDates);
router.patch("/:id/privacy",
    validate(itiSchema.changePrivacySchema), itiController.changePrivacy);

// itinerary item
router.post("/:id/item",
    validate(itiSchema.addItemSchema), itiItemController.addItineraryItem);
router.patch("/:id/item/:itemId/schedule",
    validate(itiSchema.scheduleItemSchema), itiItemController.scheduleItineraryItem);
router.delete("/:id/item/:itemId",
    validate(itiSchema.deleteItemSchema), itiItemController.deleteItineraryItem);
export default router;