import { prisma } from "../libs/prisma";

const DAY_MAP: Record<string, number> = {
  Mo: 1,
  Tu: 2,
  We: 3,
  Th: 4,
  Fr: 5,
  Sa: 6,
  Su: 0,
};

export const syncOpeningHours = async (locationId: string) => {
  const loc = await prisma.location.findUnique({ where: { id: locationId } });
  if (!loc || !loc.metadata) return;

  const raw = (loc.metadata as any).opening_hours;
  if (!raw) return;

  const hoursToInsert: any[] = [];
  const toMin = (t: string): number => {
    const parts = t.trim().split(":");
    const hours = parseInt(parts[0] || "0", 10);
    const minutes = parseInt(parts[1] || "0", 10);
    return hours * 60 + minutes;
  };

  // FORMAT 3: 24/7
  if (raw.toLowerCase().includes("24/7")) {
    for (let i = 0; i < 7; i++) {
      hoursToInsert.push({ dayOfWeek: i, openTime: 0, closeTime: 1440 });
    }
  }
  // FORMAT 2: Time only (Ex: "12:00-22:00")
  else if (/^\d{2}:\d{2}-\d{2}:\d{2}$/.test(raw.trim())) {
    const [open, close] = raw.split("-");
    for (let i = 0; i < 7; i++) {
      hoursToInsert.push({
        dayOfWeek: i,
        openTime: toMin(open),
        closeTime: toMin(close),
      });
    }
  }
  // FORMAT 1: Day + Hours (Ex: "Mo-Su 06:00-21:00")
  else {
    const segments = raw.split(";"); // Split by semicolon for different day rules
    segments.forEach((seg: string) => {
      const match = seg.trim().match(/^([A-Z][a-z](?:-[A-Z][a-z])?)\s+(.+)$/);
      if (match && match[1] && match[2]) {
        const dayRange = match[1];
        const timeStr = match[2];

        const days = parseDayRange(dayRange);
        const timeSlots = timeStr.split(",");

        days.forEach((day) => {
          timeSlots.forEach((slot) => {
            const [open, close] = slot.trim().split("-");
            if (open && close) {
              hoursToInsert.push({
                dayOfWeek: day,
                openTime: toMin(open),
                closeTime: toMin(close),
              });
            }
          });
        });
      }
    });
  }

  // NeonDB Transaction: Clear old hours and insert new ones
  return await prisma.$transaction([
    prisma.locationHour.deleteMany({ where: { locationId } }),
    prisma.locationHour.createMany({
      data: hoursToInsert.map((h) => ({ ...h, locationId })),
    }),
  ]);
};

// Helper to expand "Mo-Fr" to [1,2,3,4,5]
function parseDayRange(range: string): number[] {
  const days = ["Su", "Mo", "Tu", "We", "Th", "Fr", "Sa"];

  if (!range.includes("-")) {
    const dayIndex = DAY_MAP[range];
    // Kiểm tra nếu dayIndex không tồn tại trong map
    return dayIndex !== undefined ? [dayIndex] : [];
  }

  const [start, end] = range.split("-");

  // Kiểm tra start và end có tồn tại sau khi split không
  if (!start || !end) return [];

  const startIdx = days.indexOf(start);
  const endIdx = days.indexOf(end);

  if (startIdx === -1 || endIdx === -1) return [];

  let resultDays: string[];
  if (startIdx <= endIdx) {
    resultDays = days.slice(startIdx, endIdx + 1);
  } else {
    resultDays = [...days.slice(startIdx), ...days.slice(0, endIdx + 1)];
  }

  // Sử dụng .flatMap hoặc filter để loại bỏ undefined
  return resultDays
    .map((d) => DAY_MAP[d])
    .filter((d): d is number => d !== undefined);
}
