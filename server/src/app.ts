import cors from "cors";
import express from "express";
import authRoute from "./routes/auth.route";
import userRoute from "./routes/user/index.routes";
import { requireAuth, requireRole } from "./middlewares/auth.middleware";
import {
  globalErrorHandler,
  notFoundHandler,
} from "./middlewares/error.middleware";

import userRoutes from "./routes/user/index.routes";
import adminRoutes from "./routes/admin/index.routes";

const app = express();

app.use(express.json());
app.use(express.urlencoded({ extended: true }));
app.use(cors());

// routes
// public
app.use("/api", authRoutes);

// protected
app.use(requireAuth);
app.use("/api", userRoutes);
app.use("/api/admin", adminRoutes);

app.use(notFoundHandler);
app.use(globalErrorHandler);

export default app;
