import express from "express";
import * as authController from "../controllers/auth.controller";

const router = express.Router();

router.post("/signin", authController.signIn);
router.post("/signup", authController.signUp);
router.post("/signout", authController.signOut);
router.post("/otp/verify", authController.verifyOTP);
router.post("/otp/send", authController.sendOTP);

export default router;