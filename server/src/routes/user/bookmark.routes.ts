import express from "express";
import { validate } from "../../middlewares/validate.middleware";
import {
  BookmarkQuerySchema,
  BookmarkCreateSchema,
  BookmarkParamsSchema,
} from "../../validations/bookmark.schema";
import { bookmarkController } from "../../controllers/bookmark.controller";

const router = express.Router();

router.get("/", validate(BookmarkQuerySchema), bookmarkController.getAll);
router.post("/", validate(BookmarkCreateSchema), bookmarkController.toggle);
router.delete(
  "/:id",
  validate(BookmarkParamsSchema),
  bookmarkController.delete,
);

router.get(
  "/check",
  validate(BookmarkQuerySchema),
  bookmarkController.getByLocationId,
);
export default router;
