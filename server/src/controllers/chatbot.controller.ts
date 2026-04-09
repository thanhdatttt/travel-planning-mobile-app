import { Request, Response } from "express";
import { createResponse } from "../utils/response";
import { prisma } from "../libs/prisma";
import ApiError from "../utils/apiError";
import { ChatLLMUtils } from "../utils/chatLLMUtils";

export const chatbotController = {
  async sendMessage(req: Request, res: Response) {
    const userId = req.user.id;
    const { content, sessionId } = req.body;

    if (!content) {
      throw new ApiError(400, "Message content is required");
    }

    let currentSessionId = sessionId;
    if (currentSessionId) {
      const session = await prisma.chatSession.findUnique({
        where: { id: currentSessionId },
      });

      if (!session) throw new ApiError(404, "Chat session not found");
      if (session.userId !== userId) throw new ApiError(403, "Unauthorized");
    } else {
      const newSession = await prisma.chatSession.create({
        data: {
          userId,
          title:
            content.length > 30 ? `${content.substring(0, 30)}...` : content,
        },
      });
      currentSessionId = newSession.id;
    }

    let chatHistory: any[] = [];
    if (sessionId) {
      const pastMessages = await prisma.chatMessage.findMany({
        where: { sessionId: currentSessionId },
        orderBy: { createdAt: "asc" },
      });

      chatHistory = pastMessages.map((msg) => ({
        role: msg.role,
        text: msg.content,
      }));
    }

    await prisma.chatMessage.create({
      data: {
        sessionId: currentSessionId,
        role: "user",
        content: content,
      },
    });

    const aiResponseText = await ChatLLMUtils.processChatMessage(
      content,
      chatHistory,
    );

    const aiMessage = await prisma.chatMessage.create({
      data: {
        sessionId: currentSessionId,
        role: "model",
        content: aiResponseText,
      },
    });

    return res.status(200).json(
      createResponse({
        message: "Message processed successfully",
        data: {
          sessionId: currentSessionId,
          message: aiMessage,
        },
      }),
    );
  },

  async getAllSessions(req: Request, res: Response) {
    const userId = req.user.id;
    const page = Number(req.query.page) || 1;
    const limit = Number(req.query.limit) || 10;
    const skip = (page - 1) * limit;

    const [sessions, total] = await Promise.all([
      prisma.chatSession.findMany({
        where: { userId },
        skip,
        take: limit,
      }),
      prisma.chatSession.count({ where: { userId } }),
    ]);

    return res.json(
      createResponse({
        data: sessions,
        metadata: {
          total,
          page,
          totalPages: Math.ceil(total / limit),
          limit: limit,
        },
      }),
    );
  },

  async getSessionMessages(req: Request, res: Response) {
    const { sessionId } = req.params as { sessionId: string };
    const userId = req.user.id;

    const session = await prisma.chatSession.findUnique({
      where: { id: sessionId },
    });

    if (!session) throw new ApiError(404, "Chat session not found");
    if (session.userId !== userId) throw new ApiError(403, "Unauthorized");

    const messages = await prisma.chatMessage.findMany({
      where: { sessionId },
      orderBy: { createdAt: "asc" },
    });

    return res.json(
      createResponse({
        data: messages,
      }),
    );
  },

  async deleteSession(req: Request, res: Response) {
    const { sessionId } = req.params as { sessionId: string };
    const userId = req.user.id;

    const session = await prisma.chatSession.findUnique({
      where: { id: sessionId },
    });

    if (!session) throw new ApiError(404, "Chat session not found");
    if (session.userId !== userId) throw new ApiError(403, "Unauthorized");

    await prisma.chatSession.delete({
      where: { id: sessionId },
    });

    return res.json(createResponse({ message: "Chat session deleted" }));
  },
};
