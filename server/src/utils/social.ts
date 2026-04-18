import { Response } from "express";
import { prisma } from "../libs/prisma";
import { genAccessToken, genRefreshToken, REFRESH_TOKEN_TTL } from "./jwt";
import { createResponse } from "./response";

interface SocialUserData {
  email: string;
  provider: "google" | "facebook";
  providerId: string;
  fullName: string;
  avatarUrl?: string | null;
}

export const updateSocialUser = async (info: SocialUserData) => {
  return await prisma.$transaction(
    async (tx) => {
      // find existing provider
      const existingProvider = await tx.authProvider.findUnique({
        where: {
          provider_providerId: {
            provider: info.provider,
            providerId: info.providerId,
          },
        },
        include: { user: true },
      });

      if (existingProvider) return existingProvider.user;

      // find existing user
      let user = await tx.user.findUnique({ where: { email: info.email } });

      if (!user) {
        // create new user
        user = await tx.user.create({
          data: {
            email: info.email,
            fullName: info.fullName,
            avatarUrl: info.avatarUrl ?? null,
            username: `${info.provider}_${info.providerId.substring(0, 5)}_${Date.now()}`,
            authProviders: {
              create: { provider: info.provider, providerId: info.providerId },
            },
          },
        });
      } else {
        // add provider if user exists
        await tx.authProvider.create({
          data: {
            provider: info.provider,
            providerId: info.providerId,
            userId: user.id,
          },
        });
      }
      return user;
    },
    {
      maxWait: 5000, // Wait 5s to get a connection
      timeout: 10000, // Allow 10s for the actual work
    },
  );
};

export const finalizeLogin = async (
  user: any,
  res: Response,
  message: string,
) => {
  // gen token
  const accessToken = genAccessToken(user.id);
  const refreshToken = genRefreshToken(user.id);
  await prisma.session.create({
    data: {
      userId: user.id,
      token: refreshToken,
      expiresAt: new Date(Date.now() + REFRESH_TOKEN_TTL),
    },
  });

  return res.status(200).json(
    createResponse({
      message: message,
      data: { userId: user.id, accessToken, refreshToken },
    }),
  );
};
