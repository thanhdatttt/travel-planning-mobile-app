import express from "express";
import * as authController from "../controllers/auth.controller";
import { validate } from "../middlewares/validate.middleware";
import { userSignInSchema, userSignUpSchema } from "../validations/auth.validation";

const router = express.Router();

// basic authentication
router.post("/signin", validate(userSignInSchema), authController.signIn);
router.post("/signup", validate(userSignUpSchema), authController.signUp);
router.post("/signout", authController.signOut);

// email verification
router.post("/otp/verify", authController.verifyOTP);
router.post("/otp/send", authController.sendOTP);

// oauth
router.post("/google", authController.googleAuth);
router.post("/facebook", authController.facebookAuth);
export default router;