-- AddForeignKey
ALTER TABLE "Favorite" ADD CONSTRAINT "Favorite_itineraryId_fkey" FOREIGN KEY ("itineraryId") REFERENCES "Itinerary"("id") ON DELETE CASCADE ON UPDATE CASCADE;
