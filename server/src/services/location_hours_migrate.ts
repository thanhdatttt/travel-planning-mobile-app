import { prisma } from "../libs/prisma";
import { Prisma } from "../generated/prisma/client";
import { syncOpeningHours } from "./location.hours.parse";
export async function runMigration() {
  console.log("Bắt đầu migrate giờ mở cửa...");

  const locations = await prisma.location.findMany({
    where: {
      metadata: {
        path: ["opening_hours"],
        not: Prisma.DbNull,
      },
    },
  });

  console.log(`Found ${locations.length} locations to sync.`);

  for (const loc of locations) {
    try {
      await syncOpeningHours(loc.id);
      console.log(`Synced: ${loc.name}`);
    } catch (error) {
      console.error(`Failed: ${loc.name}`, error);
    }
  }

  console.log("Migration hoàn tất!");
}