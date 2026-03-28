/*
  Warnings:

  - You are about to drop the `location_categories` table. If the table is not empty, all the data it contains will be lost.
  - You are about to drop the `locations` table. If the table is not empty, all the data it contains will be lost.

*/
-- DropForeignKey
ALTER TABLE "Bookmark" DROP CONSTRAINT "Bookmark_locationId_fkey";

-- DropForeignKey
ALTER TABLE "ItineraryItem" DROP CONSTRAINT "ItineraryItem_locationId_fkey";

-- DropForeignKey
ALTER TABLE "LocationHour" DROP CONSTRAINT "LocationHour_locationId_fkey";

-- DropForeignKey
ALTER TABLE "LocationPhoto" DROP CONSTRAINT "LocationPhoto_locationId_fkey";

-- DropForeignKey
ALTER TABLE "Review" DROP CONSTRAINT "Review_locationId_fkey";

-- DropForeignKey
ALTER TABLE "locations" DROP CONSTRAINT "locations_categoryId_fkey";

-- DropForeignKey
ALTER TABLE "locations" DROP CONSTRAINT "locations_createdBy_fkey";

-- DropTable
DROP TABLE "location_categories";

-- DropTable
DROP TABLE "locations";

-- CreateTable
CREATE TABLE "LocationCategory" (
    "id" SERIAL NOT NULL,
    "slug" TEXT NOT NULL,
    "nameEn" TEXT NOT NULL,
    "nameVi" TEXT NOT NULL,
    "icon" TEXT,
    "displayOrder" INTEGER NOT NULL DEFAULT 0,

    CONSTRAINT "LocationCategory_pkey" PRIMARY KEY ("id")
);

-- CreateTable
CREATE TABLE "Locations" (
    "id" TEXT NOT NULL,
    "osmId" TEXT,
    "isDeleted" BOOLEAN NOT NULL DEFAULT false,
    "createdBy" TEXT NOT NULL DEFAULT 'system',
    "name" TEXT NOT NULL,
    "slug" TEXT,
    "description" TEXT,
    "address" TEXT,
    "phone" TEXT,
    "website" TEXT,
    "avgRating" DOUBLE PRECISION NOT NULL DEFAULT 0,
    "ratingCount" INTEGER NOT NULL DEFAULT 0,
    "priceLevel" SMALLINT,
    "metadata" JSONB,
    "categoryId" INTEGER NOT NULL,
    "location" geography(Point, 4326) NOT NULL,
    "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updatedAt" TIMESTAMP(3) NOT NULL,
    "deletedAt" TIMESTAMP(3),

    CONSTRAINT "Locations_pkey" PRIMARY KEY ("id")
);

-- CreateIndex
CREATE UNIQUE INDEX "LocationCategory_slug_key" ON "LocationCategory"("slug");

-- CreateIndex
CREATE UNIQUE INDEX "Locations_osmId_key" ON "Locations"("osmId");

-- CreateIndex
CREATE UNIQUE INDEX "Locations_slug_key" ON "Locations"("slug");

-- CreateIndex
CREATE INDEX "Locations_name_idx" ON "Locations"("name");

-- CreateIndex
CREATE INDEX "Locations_categoryId_idx" ON "Locations"("categoryId");

-- AddForeignKey
ALTER TABLE "Locations" ADD CONSTRAINT "Locations_categoryId_fkey" FOREIGN KEY ("categoryId") REFERENCES "LocationCategory"("id") ON DELETE RESTRICT ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "Locations" ADD CONSTRAINT "Locations_createdBy_fkey" FOREIGN KEY ("createdBy") REFERENCES "User"("id") ON DELETE CASCADE ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "LocationHour" ADD CONSTRAINT "LocationHour_locationId_fkey" FOREIGN KEY ("locationId") REFERENCES "Locations"("id") ON DELETE CASCADE ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "LocationPhoto" ADD CONSTRAINT "LocationPhoto_locationId_fkey" FOREIGN KEY ("locationId") REFERENCES "Locations"("id") ON DELETE CASCADE ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "Review" ADD CONSTRAINT "Review_locationId_fkey" FOREIGN KEY ("locationId") REFERENCES "Locations"("id") ON DELETE CASCADE ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "Bookmark" ADD CONSTRAINT "Bookmark_locationId_fkey" FOREIGN KEY ("locationId") REFERENCES "Locations"("id") ON DELETE CASCADE ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "ItineraryItem" ADD CONSTRAINT "ItineraryItem_locationId_fkey" FOREIGN KEY ("locationId") REFERENCES "Locations"("id") ON DELETE RESTRICT ON UPDATE CASCADE;
