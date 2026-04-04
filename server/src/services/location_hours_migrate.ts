import { prisma } from "../libs/prisma"; // Đường dẫn tới file khởi tạo prisma của bạn
import { Prisma } from '@prisma/client';
// Sau đó sử dụng: PrismaClient.Prisma.DbNull
import { syncOpeningHours } from "./location.hours.parse";
async function runMigration() {
  console.log("🚀 Bắt đầu migrate giờ mở cửa...");

  // Lấy tất cả các ID của Location có metadata chứa opening_hours
  const locations = await prisma.location.findMany({
    where: {
      metadata: {
        path: ["opening_hours"],
        not: Prisma.DbNull, // Hoặc Prisma.AnyNull nếu muốn bao quát cả 2 loại null
      },
    },
  });

  console.log(`Found ${locations.length} locations to sync.`);

  for (const loc of locations) {
    try {
      await syncOpeningHours(loc.id);
      console.log(`✅ Synced: ${loc.name}`);
    } catch (error) {
      console.error(`❌ Failed: ${loc.name}`, error);
    }
  }

  console.log("🏁 Migration hoàn tất!");
}

runMigration();
