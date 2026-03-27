import cors from "cors";
import express from "express";
import authRoute from "./routes/auth/index.routes";
import userRoute from "./routes/user/index.routes";
import { requireAuth, requireRole } from "./middlewares/auth.middleware";
import {
  globalErrorHandler,
  notFoundHandler,
} from "./middlewares/error.middleware";
import adminRoutes from "./routes/admin/index.routes";
import itineraryRoutes from "./routes/itinerary/index.routes";

const app = express();

app.use(express.json());
app.use(express.urlencoded({ extended: true }));
app.use(cors());

// routes
// public
app.use("/api", authRoute);

// protected
app.use(requireAuth);
app.use("/api", userRoutes);
app.use("/api/admin", requireRole("admin"), adminRoutes);
app.use("/api", itineraryRoutes);

app.use(notFoundHandler);
app.use(globalErrorHandler);

export default app;
