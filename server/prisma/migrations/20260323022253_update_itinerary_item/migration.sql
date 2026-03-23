/*
  Warnings:

  - You are about to drop the column `days` on the `ItineraryItem` table. All the data in the column will be lost.

*/
-- DropIndex
DROP INDEX "ItineraryItem_itineraryId_orderIdx_key";

-- AlterTable
ALTER TABLE "ItineraryItem" DROP COLUMN "days",
ADD COLUMN     "dayNumber" INTEGER,
ALTER COLUMN "startTime" DROP NOT NULL,
ALTER COLUMN "startTime" DROP DEFAULT,
ALTER COLUMN "endTime" DROP NOT NULL,
ALTER COLUMN "endTime" DROP DEFAULT;

-- CreateIndex
CREATE INDEX "ItineraryItem_dayNumber_idx" ON "ItineraryItem"("dayNumber");
