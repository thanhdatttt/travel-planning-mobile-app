import { Request, Response } from "express";
import { prisma } from "../../libs/prisma";
import { createResponse } from "../../utils/response";

const getDaysInMonth = (year: number, month: number) => new Date(year, month, 0).getDate();

export const getAdminStats = async (req: Request, res: Response) => {
    const now = new Date();
    const month = Number(req.query.month) || now.getMonth() + 1;
    const year = Number(req.query.year) || now.getFullYear();

    const startDate = new Date(year, month - 1, 1);
    const endDate = new Date(year, month, 0, 23, 59, 59);

    // Thống kê Session (Sign-ins)
    const sessions = await prisma.session.findMany({
        where: { createdAt: { gte: startDate, lte: endDate } },
        select: { createdAt: true }
    });

    // Thống kê Reviews
    const reviews = await prisma.review.findMany({
        where: { createdAt: { gte: startDate, lte: endDate }, isDeleted: false },
        select: { createdAt: true }
    });

    // Đếm số lượng Role
    const userCounts = await prisma.user.groupBy({
        by: ['role'],
        _count: { id: true }
    });

    const groupByDay = (data: any[]) => {
        const days = getDaysInMonth(year, month);
        const map = new Array(days).fill(0);
        data.forEach(item => {
            const day = new Date(item.createdAt).getDate();
            map[day - 1]++;
        });
        return map.map((count, index) => ({ label: `${index + 1}`, value: count }));
    };

    return res.status(200).json(createResponse({
        message: "Stats retrieved successfully",
        data: {
            signInData: groupByDay(sessions),
            reviewData: groupByDay(reviews),
            counts: {
                user: userCounts.find(c => c.role === 'user')?._count.id || 0,
                moderator: userCounts.find(c => c.role === 'moderator')?._count.id || 0,
                admin: userCounts.find(c => c.role === 'admin')?._count.id || 0,
            }
        }
    }));
};