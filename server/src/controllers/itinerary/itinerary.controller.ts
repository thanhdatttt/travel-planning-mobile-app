import { prisma } from "../../libs/prisma";
import { Request, Response } from "express";
import { createResponse } from "../../utils/response";

export const createItinerary = async (req: Request, res: Response) => {
  try {
    const data: {
      title: string;
      startDate: Date;
      endDate: Date;
    } = req.body;
    const userId: string = req.user.id;

    const itinerary = await prisma.itinerary.create({
      data: {
        ownerId: userId,
        title: data.title,
        startDate: new Date(data.startDate),
        endDate: new Date(data.endDate),
      },
    });

    return res.status(200).json(
      createResponse({
        message: "Create itinerary successfully",
        data: itinerary,
      }),
    );

  } catch(err: any) {
      console.log("Error when creating itinerary: ", err.message);
      return res
        .status(500)
        .json(createResponse({ message: "System error", error: err.message }));
  }
}

export const getItinerary = async (req: Request, res: Response) => {
  try {
    const { id } = req.params;
    if (!id) {
      return res
        .status(400)
        .json(createResponse({ message: "Bad request", error: "Missing id" }));
    }

    const itinerary = await prisma.itinerary.findUnique({
      where: { id: String(id) },
      include: {
        itineraryItems: {
          orderBy: [
            { dayNumber: 'asc' },
            { orderIdx: 'asc' }
          ],
          include: { location: true }
        }
      }
    });
    if (!itinerary) {
      return res
        .status(404)
        .json(createResponse({ message: "Not found", error: "Itinerary ot found" }));
    }

    return res.status(200).json(
      createResponse({
        message: "Get itineraries successfully",
        data: itinerary,
      }),
    );
  } catch (err: any) {
    console.log("Error when getting itinerary: ", err.message);
    return res
      .status(500)
      .json(createResponse({ message: "System error", error: err.message }));
  }
}

export const deleteItinerary = async (req: Request, res: Response) => {
  try {
    const { id } = req.params;
    const userId: string = req.user.id;
    if (!id) {
      return res
        .status(400)
        .json(createResponse({ message: "Bad request", error: "Missing id" }));
    }

    // only owner can delete itinerary
    const itinerary = await prisma.itinerary.delete({
      where: { id: String(id), ownerId: userId },
    });
    if (!itinerary) {
      return res
        .status(404)
        .json(createResponse({ message: "Not found", error: "Itinerary not found" }));
    }

    return res.status(200).json(
      createResponse({
        message: "Delete itinerary successfully",
        data: itinerary,
      }),
    );
  } catch (err: any) {
    console.log("Error when deleting itinerary: ", err.message);
    return res
      .status(500)
      .json(createResponse({ message: "System error", error: err.message }));
  }
}

export const updateItineraryTime = async (req: Request, res: Response) => {
  try {
    const { id } = req.params;
    const data: {
      startDate: Date;
      endDate: Date;
    } = req.body;
    if (!id) {
      return res
        .status(400)
        .json(createResponse({ message: "Bad request", error: "Missing id" }));
    }

    const itinerary = await prisma.itinerary.update({
      where: { id: String(id) },
      data: {
        startDate: new Date(data.startDate),
        endDate: new Date(data.endDate),
      },
    });
    if (!itinerary) {
      return res
        .status(404)
        .json(createResponse({ message: "Not found", error: "Itinerary not found" }));
    }

    return res.status(200).json(
      createResponse({
        message: "Update itinerary time successfully",
        data: itinerary,
      }),
    );
  } catch (err: any) {
    console.log("Error when updating itinerary time: ", err.message);
    return res
      .status(500)
      .json(createResponse({ message: "System error", error: err.message }));
  }
}