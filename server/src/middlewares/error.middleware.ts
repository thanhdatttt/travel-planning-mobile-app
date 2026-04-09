import { Request, Response, NextFunction } from "express";
import { createResponse } from "../utils/response";
import ApiError from "../utils/apiError";


export const notFoundHandler = (req: Request, res: Response, next: NextFunction) => {
  const error = new Error(`Path not found: ${req.originalUrl}`);
  (error as any).status = 404;
  
  next(error);
};

export const globalErrorHandler = (err: any, req: Request, res: Response, next: NextFunction) => {
  let { statusCode, message, isOperational, stack } = err;

  if (!(err instanceof ApiError)) {
    statusCode = 500;
    message = "Internal Server Error";
  }

  console.error(`[${statusCode}] ${err.message} \n Stack: ${stack}`);

  return res.status(statusCode).json(
    createResponse({
      message: err.message,
      error: statusCode === 404 ? "Not Found" : "Bad Request"
    })
  );
};