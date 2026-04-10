import express from 'express';
import { Express, Request, Response } from "express";
import ClienteController from "./controller/ClienteController";
import { usersProducerRpc } from "./messaging/UsersProducerRPC";

const app: Express = express();
const port = process.env.PORT || 3000;

app.get("/controller/ClienteController", ClienteController);
usersProducerRpc.init();

app.listen(port, () => {
  console.log(`[server]: Server is running at http://localhost:${port}`);
});
