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
    const userId: string = req.user.id;

    const itinerary = await prisma.itinerary.findUnique({
      where: { id: String(id) },
      include: {
        itineraryItems: {
          orderBy: [
            { date: 'asc' },
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

    // check privacy
    if (itinerary.privacy === "private" && itinerary.ownerId !== userId) {
      return res
        .status(403)
        .json(createResponse({ message: "Forbidden", error: "Can not get private itinerary" }));
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

export const getUserItineraries = async (req: Request, res: Response) => {
  try {
    const userId: string = req.user.id;

    const itineraries = await prisma.itinerary.findMany({
      where: { ownerId: userId },
      include: {
        itineraryItems: {
          orderBy: [
            { date: 'asc' },
            { orderIdx: 'asc' }
          ],
        }
      }
    });

    return res.status(200).json(
      createResponse({
        message: "Get user itineraries successfully",
        data: itineraries,
      }),
    );

  } catch (err: any) {
    console.log("Error when getting user itineraries: ", err.message);
    return res
      .status(500)
      .json(createResponse({ message: "System error", error: err.message }));
  }
}

export const deleteItinerary = async (req: Request, res: Response) => {
  try {
    const { id } = req.params;
    const userId: string = req.user.id;

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
      }),
    );
  } catch (err: any) {
    console.log("Error when deleting itinerary: ", err.message);
    return res
      .status(500)
      .json(createResponse({ message: "System error", error: err.message }));
  }
}

export const updateItinerary = async (req: Request, res: Response) => {
  try {
    const { id } = req.params;
    const data: {
      title?: string;
      description?: string;
      privacy?: "public" | "private";
      startDate?: Date;
      endDate?: Date;
    } = req.body;
    const userId: string = req.user.id;

    // check exsisting itinerary and only owner can update
    const existingItinerary = await prisma.itinerary.findUnique({
      where: { id: String(id) },
    });
    if (!existingItinerary) {
      return res.status(404).json(
        createResponse({ message: "Not found", error: "Itinerary not found" })
      );
    }
    if (existingItinerary.ownerId !== userId) {
      return res
        .status(403)
        .json(createResponse({ message: "Forbiden", error: "Itinerary not yours" }));
    }

    // update data
    const updateData: any = {};
    if (data.title !== undefined) updateData.title = data.title;
    if (data.description !== undefined) updateData.description = data.description;
    if (data.privacy !== undefined) updateData.privacy = data.privacy;
    if (data.startDate !== undefined) updateData.startDate = new Date(data.startDate);
    if (data.endDate !== undefined) updateData.endDate = new Date(data.endDate);
    const updatedItinerary = await prisma.itinerary.update({
      where: { id: String(id) },
      data: updateData,
    });

    // unschedule items if start date or end date changed
    if (data.startDate || data.endDate) {
      await prisma.itineraryItem.updateMany({
        where: {
          itineraryId: String(id),
          OR: [
            { date: { lt: updatedItinerary.startDate } },
            { date: { gt: updatedItinerary.endDate } }
          ]
        },
        data: {
          date: null,
          orderIdx: 0
        }
      });
    }

    return res.status(200).json(
      createResponse({
        message: "Update itinerary successfully",
        data: updatedItinerary,
      }),
    );


  } catch (err: any) {
    console.log("Error when updating itinerary: ", err.message);
    return res
      .status(500)
      .json(createResponse({ message: "System error", error: err.message }));
  }
}