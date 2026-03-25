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
    validate(itiSchema.idParamSchema), itiController.getItinerary);
router.get("/", itiController.getUserItineraries);
router.delete("/:id", 
    validate(itiSchema.idParamSchema), itiController.deleteItinerary);
router.patch("/:id", 
    validate(itiSchema.updateItinerarySchema), itiController.updateItinerary);

// itinerary item
router.post("/:id/item",
    validate(itiSchema.addItemSchema), itiItemController.addItineraryItem);
router.delete("/:id/item/:itemId",
    validate(itiSchema.idItemParamSchema), itiItemController.deleteItineraryItem);
router.patch("/:id/item/:itemId/schedule",
    validate(itiSchema.scheduleItemSchema), itiItemController.scheduleItineraryItem);
router.patch("/:id/item/:itemId/unschedule",
    validate(itiSchema.idItemParamSchema), itiItemController.unscheduleItineraryItem);
    
export default router;