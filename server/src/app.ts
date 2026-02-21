import cookieParser from "cookie-parser";
import cors from "cors";
import express from "express";
import authRoute from "./routes/auth.route";
import userRoute from "./routes/user.route";
import { requireAuth } from "./middlewares/auth.middleware";

const app = express();

app.use(express.json());
app.use(express.urlencoded({extended: true}));
app.use(cookieParser());
app.use(cors());

// routes
app.use("/api/auth", authRoute);

app.use(requireAuth);
app.use("/api/users", userRoute);

export default app;