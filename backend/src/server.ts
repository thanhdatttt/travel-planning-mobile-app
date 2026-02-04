import app from "./app";
import http from "node:http";
import { config } from "./configs/config";


const server = http.createServer(app);
server.listen(config.PORT, () => {
    console.log(`Server is running on PORT: ${config.PORT}...`);
});