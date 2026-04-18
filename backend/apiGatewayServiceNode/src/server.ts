import express from 'express';
import { Express, Request, Response } from "express";
import ClienteController from "./controller/ClienteController";
import GerenteController from "./controller/GerenteController";
import { GerentesProducer, usersProducer } from './messaging/GenericProducerRPC';

const app: Express = express();
const port = process.env.PORT || 3000;
export const rabbitmqUrl = "amqp://usuario:admin@rabbitmq";

//passei um tempo enorme tentando entender por que o orquestrator tava mandando como se a menssagem fosse null
//era por causa dessa linha aq
app.use(express.json());
app.use(express.urlencoded({ extended: true }));

app.use("/clientes", ClienteController);
app.use("/gerentes", GerenteController);
usersProducer.init();
GerentesProducer.init();

app.listen(port, () => {
  console.log(`[server]: Server is running at http://localhost:${port}`);
});
