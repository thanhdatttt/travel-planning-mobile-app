import cookieParser from "cookie-parser";
import cors from "cors";
import express from "express";
import authRoute from "./routes/auth.route";
import userRoute from "./routes/user/index.routes";
import { requireAuth, requireRole } from "./middlewares/auth.middleware";

import userRoutes from "./routes/user/index.routes"
import adminRoutes from "./routes/admin/index.routes"

const app = express();

app.use(express.json());
app.use(express.urlencoded({ extended: true }));
app.use(cookieParser());
app.use(cors());

// routes
app.use("/api/auth", authRoute);

app.use(requireAuth);
app.use("/api", userRoutes);
app.use("/api/admin", requireRole('admin'), adminRoutes);


export default app;
