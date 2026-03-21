/*
  Warnings:

  - The `type` column on the `Location` table would be dropped and recreated. This will lead to data loss if there is data in the column.
  - A unique constraint covering the columns `[osmId]` on the table `Location` will be added. If there are existing duplicate values, this will fail.
  - A unique constraint covering the columns `[slug]` on the table `Location` will be added. If there are existing duplicate values, this will fail.

*/
-- DropIndex
DROP INDEX "LocationPhoto_uploaderId_locationId_key";

-- AlterTable
ALTER TABLE "Location" ADD COLUMN     "osmId" TEXT,
ALTER COLUMN "createdBy" SET DEFAULT 'system',
ALTER COLUMN "description" DROP NOT NULL,
ALTER COLUMN "address" DROP NOT NULL,
ALTER COLUMN "phone" DROP NOT NULL,
ALTER COLUMN "priceLevel" DROP NOT NULL,
DROP COLUMN "type",
ADD COLUMN     "type" TEXT NOT NULL DEFAULT 'attraction';

-- AlterTable
ALTER TABLE "User" ADD COLUMN     "dob" TIMESTAMP(3);

-- CreateIndex
CREATE UNIQUE INDEX "Location_osmId_key" ON "Location"("osmId");

-- CreateIndex
CREATE UNIQUE INDEX "Location_slug_key" ON "Location"("slug");
