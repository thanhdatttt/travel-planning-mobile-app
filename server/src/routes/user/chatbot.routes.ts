import { chatbotController } from "../../controllers/chatbot.controller";
import express from "express";

const router = express.Router();

router.post("/message", chatbotController.sendMessage);
router.get("/sessions", chatbotController.getAllSessions);
router.get(
  "/sessions/:sessionId/messages",
  chatbotController.getSessionMessages,
);
router.delete("/sessions/:sessionId", chatbotController.deleteSession);

export default router;
