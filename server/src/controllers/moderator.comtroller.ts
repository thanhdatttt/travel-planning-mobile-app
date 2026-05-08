import { Request, Response } from "express";
import { createResponse } from "../utils/response";
import ApiError from "../utils/apiError";
import { prisma } from "../libs/prisma";

// Helper function to extract unique user IDs to fetch them all at once
const getUniqueUserIds = (ids: (string | null | undefined)[]) => {
  return [...new Set(ids.filter(Boolean))] as string[];
};

export const moderatorController = {
  async getReportsReview(req: Request, res: Response) {
      const skip = parseInt(req.query.skip as string) || 0;
      const take = parseInt(req.query.take as string) || 20;

      const reports = await prisma.report.findMany({
        where: { targetType: "review", status: "pending" },
        skip,
        take,
        orderBy: { createdAt: "desc" },
      });

      if (reports.length === 0) {
        return res.status(200).json(createResponse({ message: "Success", data: [], metadata: { total: 0, skip, take } }));
      }

      const reviewIds = reports.map(report => report.targetId);
      const reviews = await prisma.review.findMany({
        where: { id: { in: reviewIds } },
        select: { id: true, title: true, body: true, rating: true, userId: true, createdAt: true }
      });
      const reviewMap = new Map(reviews.map(review => [review.id, review]));

      // Fetch users (both reporters and reviewers)
      const userIds = getUniqueUserIds([...reports.map(r => r.reporterId), ...reviews.map(r => r.userId)]);
      const users = await prisma.user.findMany({
          where: { id: { in: userIds } },
          select: { id: true, username: true, avatarUrl: true }
      });
      const userMap = new Map(users.map(u => [u.id, u]));

      // Flatten into ReviewReportResponse
      const result = reports.map(report => {
        const review = reviewMap.get(report.targetId);
        const reporter = userMap.get(report.reporterId);
        const reviewer = review ? userMap.get(review.userId) : null;

        return {
          reportId: report.id,
          reviewId: review?.id || null,
          reporterId: report.reporterId,
          reviewerId: review?.userId || null,
          reviewerUsername: reviewer?.username || "Unknown",
          reviewerAvatarUrl: reviewer?.avatarUrl || null,
          reviewDate: review?.createdAt.toISOString() || "",
          rating: review?.rating || 0,
          reviewTitle: review?.title || "",
          reviewBody: review?.body || "",
          reporterUsername: reporter?.username || "Unknown",
          reportReason: report.reason
        };
      });

      return res.status(200).json(
        createResponse({ message: "Reports fetched successfully", data: result, metadata: { total: result.length, skip, take } })
      );
  },

  async getReportsLocation(req: Request, res: Response) {
      const skip = parseInt(req.query.skip as string) || 0;
      const take = parseInt(req.query.take as string) || 20;

      const reports = await prisma.report.findMany({
        where: { targetType: "location", status: "pending" },
        skip,
        take,
        orderBy: { createdAt: "desc" },
        include: {
        user: {
            select: { username: true, fullName: true }
          }
        },
      });

      if (reports.length === 0) {
        return res.status(200).json(createResponse({ message: "Success", data: [], metadata: { total: 0, skip, take } }));
      }

      const locationIds = reports.map(report => report.targetId);
      const locations = await prisma.location.findMany({
        where: { id: { in: locationIds } },
        select: {
          id: true, name: true, description: true, address: true,
          locationPhotos: { select: { url: true }, take: 1, orderBy: { isFeature: 'desc' } }
        }
      });
      const locationMap = new Map(locations.map(location => [location.id, location]));

      // Flatten into LocationReportResponse
      const result = reports.map(report => {
        const loc = locationMap.get(report.targetId);
        
        return {
          reportId: report.id,
          reporterId: report.reporterId,
          targetId: report.targetId,
          reason: report.reason,
          reporterName: report.user?.username || report.user?.fullName ||  "Unknown User",
          photoURL: loc?.locationPhotos?.[0]?.url || null,
          locationName: loc?.name || "Unknown",
          locationAddress: loc?.address || "No address",
          locationDescription: loc?.description || "",
          handledBy: report.handledBy,
          createdAt: report.createdAt.toISOString()
        };
      });

      console.log(result);
      
      return res.status(200).json(
        createResponse({ message: "Reports fetched successfully", data: result, metadata: { total: result.length, skip, take } })
      );
  },
  
  async getReportsItinerary(req: Request, res: Response) {
      const skip = parseInt(req.query.skip as string) || 0;
      const take = parseInt(req.query.take as string) || 20;

      const reports = await prisma.report.findMany({
        where: { targetType: "itinerary", status: "pending" },
        skip,
        take,
        orderBy: { createdAt: "desc" },
      });

      if (reports.length === 0) {
        return res.status(200).json(createResponse({ message: "Success", data: [], metadata: { total: 0, skip, take } }));
      }

      const itineraryIds = reports.map(report => report.targetId);
      const itineraries = await prisma.itinerary.findMany({
        where: { id: { in: itineraryIds } },
        select: { id: true, title: true, description: true, ownerId: true, privacy: true, startDate: true, endDate: true }
      });
      const itineraryMap = new Map(itineraries.map(itinerary => [itinerary.id, itinerary]));

      // Fetch users (both reporters and owners)
      const userIds = getUniqueUserIds([...reports.map(r => r.reporterId), ...itineraries.map(i => i.ownerId)]);
      const users = await prisma.user.findMany({
          where: { id: { in: userIds } },
          select: { id: true, username: true, avatarUrl: true }
      });
      const userMap = new Map(users.map(u => [u.id, u]));

      // Flatten into ItineraryReportResponse
      const result = reports.map(report => {
        const itinerary = itineraryMap.get(report.targetId);
        const reporter = userMap.get(report.reporterId);
        const owner = itinerary ? userMap.get(itinerary.ownerId) : null;

        return {
          reportId: report.id,
          itineraryId: itinerary?.id || null,
          reporterId: report.reporterId,
          ownerId: itinerary?.ownerId || null,
          ownerAvatarUrl: owner?.avatarUrl || null,
          itineraryTitle: itinerary?.title || "Unknown",
          privacy: itinerary?.privacy || "private",
          startDate: itinerary?.startDate?.toISOString() || "",
          endDate: itinerary?.endDate?.toISOString() || "",
          description: itinerary?.description || "",
          reporterUsername: reporter?.username || "Unknown",
          reportReason: report.reason
        };
      });
      
      return res.status(200).json(
        createResponse({ message: "Reports fetched successfully", data: result, metadata: { total: result.length, skip, take } })
      );
  },

  async banUser(req: Request, res: Response) {
    const { id } = req.params;
    const { ban } = req.body; 

    const updatedUser = await prisma.user.update({
      where: { id: String(id) },
      data: { isBanned: Boolean(ban) },
    });

    return res.status(200).json(
      createResponse({
        message: `User ${ban ? 'banned' : 'unbanned'} successfully`,
        data: updatedUser,
      })
    );
  },
  
  async dismissReport(req: Request, res: Response) {
        const reportId = req.params.id as string;
        const moderatorId = req.user.id; 

        const existingReport = await prisma.report.findUnique({
            where: { id: reportId }
        });

        if (!existingReport) {
            return res.status(404).json({ success: false, message: "Report not found" });
        }

        if (existingReport.status === 'processed') {
            return res.status(400).json({ success: false, message: "Report is already processed" });
        }

        const updatedReport = await prisma.report.update({
            where: { id: reportId },
            data: {
                status: 'processed',
                handledAt: new Date(),
                handledBy: moderatorId
            }
        });

        res.status(200).json(
            createResponse({
                message: "Report dismissed successfully",
                data: updatedReport
            })
        );
  },
};