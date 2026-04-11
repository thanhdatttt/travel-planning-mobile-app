import express from "express";

import { categoryController } from "../../controllers/category.controller";

const router = express.Router();

router.get("/", categoryController.getCategories);

export default router;
