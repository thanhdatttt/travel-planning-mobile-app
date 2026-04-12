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

    return res.status(201).json(
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
        },
      }
    });
    if (!itinerary) {
      return res
        .status(404)
        .json(createResponse({ message: "Not found", error: "Itinerary not found" }));
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

    // pagination
    const page = parseInt(req.query.page as string) || 1;
    const limit = parseInt(req.query.limit as string) || 10;
    const skip = (page - 1) * limit;

    const [itineraries, total] = await Promise.all([
      prisma.itinerary.findMany({
        where: { ownerId: userId },
        skip: skip,
        take: limit,
        orderBy: { createdAt: 'desc' },
        include: {
          itineraryItems: {
            orderBy: [
              { date: 'asc' },
              { orderIdx: 'asc' }
            ],
            take: 1, // only take 1 for preview
            include: { location: true }
          }
        }
      }),
      prisma.itinerary.count({ where: { ownerId: userId } })
    ]);

    return res.status(200).json(
      createResponse({
        message: "Get user itineraries successfully",
        data: {
          items: itineraries,
          metadata: {
            currentPage: page,
            limit: limit,
            totalItems: total,
            totalPages: Math.ceil(total / limit),
          }},
      }),
    );

  } catch (err: any) {
    console.log("Error when getting user itineraries: ", err.message);
    return res
      .status(500)
      .json(createResponse({ message: "System error", error: err.message }));
  }
}

export const getPublicItineraries = async (req: Request, res: Response) => {
  try {
    // pagination
    const page = parseInt(req.query.page as string) || 1;
    const limit = parseInt(req.query.limit as string) || 10;
    const skip = (page - 1) * limit;

    const [itineraries, total] = await Promise.all([
      prisma.itinerary.findMany({
        where: { privacy: "public", ownerId: { not: req.user.id } },
        skip: skip,
        take: limit,
        orderBy: { updatedAt: 'desc' },
        include: {
          itineraryItems: {
            orderBy: [
              { date: 'asc' },
              { orderIdx: 'asc' }
            ],
            take: 1, // only take 1 for preview
            include: { location: true }
          }
        }
      }),
      prisma.itinerary.count({ where: { privacy: "public" } })
    ]);

    return res.status(200).json(
      createResponse({
        message: "Get public itineraries successfully",
        data: {
          items: itineraries,
          metadata: {
            currentPage: page,
            limit: limit,
            totalItems: total,
            totalPages: Math.ceil(total / limit),
          }
        },
      }),
    );
  } catch (err: any) {
    console.log("Error when getting public itineraries: ", err.message);
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
    const itinerary = await prisma.itinerary.findUnique({
      where: { id: String(id) },
    });
    if (!itinerary) {
      return res
        .status(404)
        .json(createResponse({ message: "Not found", error: "Itinerary not found" }));
    }
    if (itinerary.ownerId !== userId) {
      return res.status(403).json(createResponse({ message: "Forbidden", error: "Itinerary not yours" }));
    }

    await prisma.itinerary.delete({ where: { id: String(id) } });

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
        .json(createResponse({ message: "Forbiddden", error: "Itinerary not yours" }));
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
      // find out-of-range items
      const outOfRangeItems = await prisma.itineraryItem.findMany({
        where: {
          itineraryId: String(id),
          date: { not: null },
          OR: [
            { date: { lt: updatedItinerary.startDate } },
            { date: { gt: updatedItinerary.endDate } },
          ],
        },
        orderBy: { orderIdx: 'asc' },
      });

      if (outOfRangeItems.length > 0) {
        await prisma.$transaction(
          outOfRangeItems.map((item) =>
            prisma.itineraryItem.update({
              where: { id: item.id },
              data: { date: null, orderIdx: null },
            })
          )
        );
      }
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

export const cloneItinerary = async (req: Request, res: Response) => {
  try {
    const { id } = req.params;
    const userId: string = req.user.id;

    const originalItinerary = await prisma.itinerary.findUnique({
      where: { id: String(id) },
      include: { itineraryItems: true }
    });
    if (!originalItinerary) {
      return res
        .status(404)
        .json(createResponse({ message: "Not found", error: "Itinerary not found" }));
    }

    // check privacy
    if (originalItinerary.privacy === "private" && originalItinerary.ownerId !== userId) {
      return res
        .status(403)
        .json(createResponse({ message: "Forbidden", error: "Cannot clone a private itinerary" }));
    }

    // clone
    const cloneItinerary = await prisma.itinerary.create({
      data: {
        ownerId: userId,
        title: originalItinerary.title,
        startDate: originalItinerary.startDate,
        endDate: originalItinerary.endDate,
        description: originalItinerary.description,
        privacy: originalItinerary.privacy,
        itineraryItems: {
          create:
            originalItinerary.itineraryItems.map((item) => ({
              date: item.date,
              locationId: item.locationId,
              orderIdx: item.orderIdx,
            })),
        },
      },
      include: { itineraryItems: true }
    });

    return res.status(200).json(
      createResponse({
        message: "Clone itinerary successfully",
        data: cloneItinerary,
      }),
    );
  } catch (err: any) {
    console.log("Error when cloning itinerary: ", err.message);
    return res
      .status(500)
      .json(createResponse({ message: "System error", error: err.message }));
  }
}