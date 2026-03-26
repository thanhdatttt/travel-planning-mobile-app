/*
  Warnings:

  - You are about to drop the column `dayNumber` on the `ItineraryItem` table. All the data in the column will be lost.

*/
-- DropIndex
DROP INDEX "ItineraryItem_itineraryId_dayNumber_orderIdx_idx";

-- AlterTable
ALTER TABLE "ItineraryItem" DROP COLUMN "dayNumber",
ADD COLUMN     "date" TIMESTAMP(3);

-- CreateIndex
CREATE INDEX "ItineraryItem_itineraryId_date_orderIdx_idx" ON "ItineraryItem"("itineraryId", "date", "orderIdx");
