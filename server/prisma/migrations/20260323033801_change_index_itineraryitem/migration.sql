-- DropIndex
DROP INDEX "ItineraryItem_dayNumber_idx";

-- DropIndex
DROP INDEX "ItineraryItem_itineraryId_idx";

-- AlterTable
ALTER TABLE "ItineraryItem" ALTER COLUMN "orderIdx" SET DEFAULT 0;

-- CreateIndex
CREATE INDEX "ItineraryItem_itineraryId_dayNumber_orderIdx_idx" ON "ItineraryItem"("itineraryId", "dayNumber", "orderIdx");
