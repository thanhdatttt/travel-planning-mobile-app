/*
  Warnings:

  - You are about to drop the column `note` on the `LocationHour` table. All the data in the column will be lost.

*/
-- AlterTable
ALTER TABLE "Location" ADD COLUMN     "email" TEXT;

-- AlterTable
ALTER TABLE "LocationHour" DROP COLUMN "note";
