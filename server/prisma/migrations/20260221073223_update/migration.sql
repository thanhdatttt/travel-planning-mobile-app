/*
  Warnings:

  - A unique constraint covering the columns `[token]` on the table `Session` will be added. If there are existing duplicate values, this will fail.
  - Made the column `providerId` on table `AuthProvider` required. This step will fail if there are existing NULL values in that column.

*/
-- CreateEnum
CREATE TYPE "otpType" AS ENUM ('register', 'reset');

-- AlterTable
ALTER TABLE "AuthProvider" ALTER COLUMN "providerId" SET NOT NULL;

-- AlterTable
ALTER TABLE "EmailOTP" ADD COLUMN     "type" "otpType" NOT NULL DEFAULT 'register';

-- CreateIndex
CREATE INDEX "EmailOTP_expiresAt_idx" ON "EmailOTP"("expiresAt");

-- CreateIndex
CREATE UNIQUE INDEX "Session_token_key" ON "Session"("token");
