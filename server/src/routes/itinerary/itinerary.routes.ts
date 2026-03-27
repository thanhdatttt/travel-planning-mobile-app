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
router.get("/me",
    validate(itiSchema.paginationSchema), itiController.getUserItineraries);
router.get("/public",
    validate(itiSchema.paginationSchema), itiController.getPublicItineraries);
router.delete("/:id", 
    validate(itiSchema.idParamSchema), itiController.deleteItinerary);
router.patch("/:id", 
    validate(itiSchema.updateItinerarySchema), itiController.updateItinerary);
router.post("/:id/clone", 
    validate(itiSchema.idParamSchema), itiController.cloneItinerary);

// itinerary item
router.post("/:id/item",
    validate(itiSchema.addItemSchema), itiItemController.addItineraryItem);
router.delete("/:id/item/:itemId",
    validate(itiSchema.idItemParamSchema), itiItemController.deleteItineraryItem);
router.patch("/:id/item/:itemId/schedule",
    validate(itiSchema.scheduleItemSchema), itiItemController.scheduleItineraryItem);
router.patch("/:id/item/:itemId/unschedule",
    validate(itiSchema.idItemParamSchema), itiItemController.unscheduleItineraryItem);
router.patch("/:id/item/:itemId/note",
    validate(itiSchema.updateItemNoteSchema), itiItemController.updateItineraryItemNote); 

export default router;