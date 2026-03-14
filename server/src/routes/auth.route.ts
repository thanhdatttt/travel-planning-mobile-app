import express from "express";
import * as authController from "../controllers/auth.controller";
import { validate } from "../middlewares/validate.middleware";
import {
  resetPasswordSchema,
  userSignInSchema,
  userSignUpSchema,
} from "../validations/auth.schema";

const router = express.Router();

// basic authentication
router.post("/signin", validate(userSignInSchema), authController.signIn);
router.post("/signup", validate(userSignUpSchema), authController.signUp);
router.post("/signout", authController.signOut);
router.post("/refresh", authController.refreshToken);

// otp
router.post("/otp/verify", authController.verifyOTP);
router.post("/otp/send", authController.sendOTP);

// reset password
router.post(
  "/reset-password",
  validate(resetPasswordSchema),
  authController.resetPassword,
);

// oauth
router.post("/google", authController.googleAuth);
router.post("/facebook", authController.facebookAuth);

export default router;
