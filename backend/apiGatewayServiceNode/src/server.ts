import express from 'express';
import { Express, Request, Response } from "express";
import ClienteController from "./controller/ClienteController";
import { usersProducerRpc } from "./messaging/UsersProducerRPC";

const app: Express = express();
const port = process.env.PORT || 3000;

//passei um tempo enorme tentando entender por que o orquestrator tava mandando como se a menssagem fosse null
//era por causa dessa linha aq
app.use(express.json());
app.use(express.urlencoded({ extended: true }));

app.use("/clientes", ClienteController);
usersProducerRpc.init();

app.listen(port, () => {
  console.log(`[server]: Server is running at http://localhost:${port}`);
});
