import { prisma } from "../../libs/prisma";
import { Request, Response } from "express";
import { createResponse } from "../../utils/response";

export const addItineraryItem = async (req: Request, res: Response) => {
  try {
    const { id } = req.params;
    const data: {
      locationId: string;
      note?: string;
    } = req.body;
    const userId = req.user.id;

    // only owner can add
    const itinerary = await prisma.itinerary.findUnique({ where: { id: String(id) } });
    if (!itinerary || itinerary.ownerId !== userId) {
      return res
        .status(403)
        .json(createResponse({ message: "Forbiden", error: "Itinerary not yours or not found" }));
    }

    const lastItem = await prisma.itineraryItem.findFirst({
      where: { itineraryId: String(id), dayNumber: null },
      orderBy: { orderIdx: 'desc' }
    });
    const newOrderIdx = lastItem ? lastItem.orderIdx + 1 : 0;
    const itineraryItem = await prisma.itineraryItem.create({
      data: {
        itineraryId: String(id),
        locationId: data.locationId,
        note: data.note ?? null,
        orderIdx: newOrderIdx
      }
    });

    return res.status(200).json(
      createResponse({
        message: "Add itinerary item successfully",
        data: itineraryItem,
      }),
    );
  } catch (err: any) {
    console.log("Error when adding itinerary item: ", err.message);
    return res
      .status(500)
      .json(createResponse({ message: "System error", error: err.message }));
  } 
};

export const deleteItineraryItem = async (req: Request, res: Response) => {
  try {
    const { itemId } = req.params;
    const userId = req.user.id;

    // only owner can delete
    const itineraryItem = await prisma.itineraryItem.findUnique({
        where: { id: String(itemId) },
        include: { itinerary: true }
      });
    if (!itineraryItem || itineraryItem.itinerary.ownerId !== userId) {
      return res
        .status(403)
        .json(createResponse({ message: "Forbiden", error: "Itinerary item not yours or not found" }));
    }

    await prisma.itineraryItem.delete({ where: { id: String(itemId) } });

    return res.status(200).json(
      createResponse({
        message: "Delete itinerary item successfully",
      }),
    );

  } catch (err: any) {
    console.log("Error when deleting itinerary item: ", err.message);
    return res
      .status(500)
      .json(createResponse({ message: "System error", error: err.message }));
  }
};

export const scheduleItineraryItem = async (req: Request, res: Response) => {
  try {
    const { itemId } = req.params;
    const data: {
      targetDayNumber: number;
    } = req.body;
    const userId = req.user.id;

    // only owner can order
    const itineraryItem = await prisma.itineraryItem.findUnique({
        where: { id: String(itemId) },
        include: { itinerary: true }
      });
    if (!itineraryItem || itineraryItem.itinerary.ownerId !== userId) {
      return res
        .status(403)
        .json(createResponse({ message: "Forbiden", error: "Itinerary item not yours or not found" }));
    }

    const maxOrderIdxItem = await prisma.itineraryItem.findFirst({
      where: { 
        itineraryId: itineraryItem.itineraryId, 
        dayNumber: data.targetDayNumber 
      },
      orderBy: { orderIdx: 'desc' }
    });
    const newOrderIdx = maxOrderIdxItem ? maxOrderIdxItem.orderIdx + 1 : 0;
    await prisma.itineraryItem.update({
      where: { id: String(itemId) },
      data: {
        dayNumber: data.targetDayNumber,
        orderIdx: newOrderIdx,
      }
    });

    return res.status(200).json(
      createResponse({
        message: "Order itinerary item successfully",
      }),
    );
  } catch (err: any) {
    console.log("Error when ordering itinerary item: ", err.message);
    return res
      .status(500)
      .json(createResponse({ message: "System error", error: err.message }));
  }
};