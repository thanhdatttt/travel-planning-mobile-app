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
    const itinerary = await prisma.itinerary.findFirst({ where: { id: String(id) } });
    if (!itinerary) {
      return res
        .status(404)
        .json(createResponse({ message: "Not found", error: "Itinerary not found" }));
    }
    if (itinerary.ownerId !== userId) {
      return res
        .status(403)
        .json(createResponse({ message: "Forbbiden", error: "Itinerary not yours" }));
    }

    const lastItem = await prisma.itineraryItem.findFirst({
      where: { itineraryId: String(id), date: null },
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

    return res.status(201).json(
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
    const itineraryItem = await prisma.itineraryItem.findFirst({
        where: { id: String(itemId) },
        include: { itinerary: true }
      });
    if (!itineraryItem) {
      return res
        .status(404)
        .json(createResponse({ message: "Not found", error: "Itinerary item not found" }));
    }
    if (itineraryItem.itinerary.ownerId !== userId) {
      return res
        .status(403)
        .json(createResponse({ message: "Forbbiden", error: "Itinerary item not yours" }));
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
      targetDate: Date;
    } = req.body;
    const userId = req.user.id;

    // only owner can order
    const itineraryItem = await prisma.itineraryItem.findFirst({
        where: { id: String(itemId) },
        include: { itinerary: true }
      });
    if (!itineraryItem) {
      return res
        .status(404)
        .json(createResponse({ message: "Not found", error: "Itinerary item not found" }));
    }
    if (itineraryItem.itinerary.ownerId !== userId) {
      return res
        .status(403)
        .json(createResponse({ message: "Forbbiden", error: "Itinerary item not yours or not found" }));
    }

    // check schedule date valid
    const { startDate, endDate } = itineraryItem.itinerary;
    const isValid = data.targetDate >= startDate && data.targetDate <= endDate;
    if (!isValid) {
      return res
        .status(400)
        .json(createResponse({ message: "Bad request", error: "Invalid schedule date" }));
    }

    const lastItemInDay = await prisma.itineraryItem.findFirst({
      where: {
        itineraryId: itineraryItem.itineraryId,
        date: data.targetDate,
      },
      orderBy: { orderIdx: 'desc' },
    });
    const newOrderIdx = lastItemInDay ? lastItemInDay.orderIdx + 1 : 0;
    const updatedItem = await prisma.itineraryItem.update({
      where: { id: String(itemId) },
      data: {
        date: data.targetDate,
        orderIdx: newOrderIdx,
      },
    });

    return res.status(200).json(
      createResponse({
        message: "Order itinerary item successfully",
        data: updatedItem,
      }),
    );
  } catch (err: any) {
    console.log("Error when ordering itinerary item: ", err.message);
    return res
      .status(500)
      .json(createResponse({ message: "System error", error: err.message }));
  }
};

export const unscheduleItineraryItem = async (req: Request, res: Response) => {
  try {
    const { itemId } = req.params;
    const userId = req.user.id;

    // only owner can order
    const itineraryItem = await prisma.itineraryItem.findFirst({
        where: { id: String(itemId) },
        include: { itinerary: true }
      });
    if (!itineraryItem) {
      return res
        .status(404)
        .json(createResponse({ message: "Not found", error: "Itinerary item not found" }));
    }
    if (itineraryItem.itinerary.ownerId !== userId) {
      return res
        .status(403)
        .json(createResponse({ message: "Forbbiden", error: "Itinerary item not yours or not found" }));
    }

    // check if date already null
    if (itineraryItem.date === null) {
      return res.status(200).json(
        createResponse({ message: "Item is already unscheduled", data: itineraryItem }),
      );
    }

    // unschedule and reorder
    const oldDate = itineraryItem.date;
    const oldOrderIdx = itineraryItem.orderIdx;
    const updatedItem = await prisma.itineraryItem.update({
      where: { id: String(itemId) },
      data: {
        date: null,
        orderIdx: 0,
      },
    });
    await prisma.itineraryItem.updateMany({
      where: {
        itineraryId: itineraryItem.itineraryId,
        date: oldDate,
        orderIdx: { gt: oldOrderIdx }
      },
      data: {
        orderIdx: { decrement: 1 }
      }
    });

    return res.status(200).json(
      createResponse({
        message: "Unschedule itinerary item successfully",
        data: updatedItem,
      }),
    );
  } catch (err: any) {
    console.log("Error when unscheduling itinerary item: ", err.message);
    return res
      .status(500)
      .json(createResponse({ message: "System error", error: err.message }));
  }
};

export const updateItineraryItemNote = async (req: Request, res: Response) => {
  try {
    const { itemId } = req.params;
    const data: {
      note: string;
    } = req.body;
    const userId: string = req.user.id;

    // only owner can update
    const itineraryItem = await prisma.itineraryItem.findFirst({
        where: { id: String(itemId) },
        include: { itinerary: true }
      });
    if (!itineraryItem) {
      return res
        .status(404)
        .json(createResponse({ message: "Not found", error: "Itinerary item not found" }));
    }
    if (itineraryItem.itinerary.ownerId !== userId) {
      return res
        .status(403)
        .json(createResponse({ message: "Forbbiden", error: "Itinerary item not yours" }));
    }

    const updatedItem = await prisma.itineraryItem.update({
      where: { id: String(itemId) },
      data: {
        note: data.note,
      },
    });

    return res.status(200).json(
      createResponse({
        message: "Update itinerary item note successfully",
        data: updatedItem,
      }),
    );
  } catch (err: any) {
    console.log("Error when updating itinerary item note: ", err.message);
    return res
      .status(500)
      .json(createResponse({ message: "System error", error: err.message }));
  }
};