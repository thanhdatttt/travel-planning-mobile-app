/*
  Warnings:

  - A unique constraint covering the columns `[itineraryId,orderIdx]` on the table `ItineraryItem` will be added. If there are existing duplicate values, this will fail.

*/
-- DropIndex
DROP INDEX "ItineraryItem_id_orderIdx_key";

-- CreateIndex
CREATE UNIQUE INDEX "ItineraryItem_itineraryId_orderIdx_key" ON "ItineraryItem"("itineraryId", "orderIdx");

-- AddForeignKey
ALTER TABLE "ItineraryItem" ADD CONSTRAINT "ItineraryItem_locationId_fkey" FOREIGN KEY ("locationId") REFERENCES "Location"("id") ON DELETE RESTRICT ON UPDATE CASCADE;
