import { createResponse } from "../utils/response";
import { Request, Response, NextFunction } from "express";
import { ZodError, ZodObject } from "zod";

interface Schema {
    body?: ZodObject,
    query?: ZodObject,
    params?: ZodObject,
}

export const validate = (schema: Schema) => async (req: Request, res: Response, next: NextFunction) => {
    try {
        if (schema.body) {
            req.body = await schema.body.parseAsync(req.body);
        }

        if (schema.query) {
            req.query = await schema.query.parseAsync(req.query) as any;
        }

        if (schema.params) {
            req.params = await schema.params.parseAsync(req.params) as any;
        }

        next();
    } catch (err: any) {
        console.log("Validate error: ", err);
        if (err instanceof ZodError) {
            return res.status(400)
            .json(createResponse({
                message: "Bad request",
                error: err.issues.map((e) => `${e.path.join(".")}: ${e.message}`),
            }));
        }

        return res.status(500).json(createResponse({message: "System error", error: err.message}));
    }
}