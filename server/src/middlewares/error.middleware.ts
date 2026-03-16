import { Request, Response, NextFunction } from "express";
import { createResponse } from "../utils/response.js";


export const notFoundHandler = (req: Request, res: Response, next: NextFunction) => {
  const error = new Error(`Path not found: ${req.originalUrl}`);
  (error as any).status = 404;
  
  next(error);
};

export const globalErrorHandler = (err: any, req: Request, res: Response, next: NextFunction) => {
  console.error(err.stack);
  console.error(err.message);

  const statusCode = err.status || err.statusCode || 500;

  return res.status(statusCode).json(
    createResponse({
      message: err.message,
    })
  );
};