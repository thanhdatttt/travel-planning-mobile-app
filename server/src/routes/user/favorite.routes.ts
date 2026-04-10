import express from "express";
import { validate } from "../../middlewares/validate.middleware";
import {
  FavoriteQuerySchema,
  FavoriteCreateSchema,
  FavoriteParamsSchema,
} from "../../validations/favorite.schema";
import { favoriteController } from "../../controllers/favorite.controller";

const router = express.Router();

router.get("/", validate(FavoriteQuerySchema), favoriteController.getAll);
router.post("/", validate(FavoriteCreateSchema), favoriteController.toggle);
router.delete("/:id", validate(FavoriteParamsSchema), favoriteController.delete);

export default router;