import express from 'express';
import { Express, Request, Response } from "express";
import cors from "cors";
import axios from "axios";
import ClienteController from "./controller/ClienteController";
import GerenteController from "./controller/GerenteController";
import ConsultaClienteController from "./controller/ConsultaClienteController";
import { authProducer, contasProducer, contasProducerCqrs, emailProducer, gerentesProducer, gerentesProducerCqrs, usersProducer, usersProducerCqrs } from './messaging/GenericProducerRPC';
import { sagaProducer } from './messaging/GenericProducer';
import AuthController from './controller/AuthController';
import emailController from "./controller/EmailController";

const app: Express = express();
const port = process.env.PORT || 3000;
export const rabbitmqUrl = "amqp://usuario:admin@rabbitmq";

//passei um tempo enorme tentando entender por que o orquestrator tava mandando como se a menssagem fosse null
//era por causa dessa linha aq
app.use(express.json());
app.use(express.urlencoded({ extended: true }));
app.use(cors());

import ContaController from "./controller/ContaController";

app.use("/clientes", ClienteController);
app.use("/gerentes", GerenteController);
app.use("/consultas", ConsultaClienteController);
app.use("/contas", ContaController);
app.use("/", AuthController);
app.use("/email", emailController);

async function startServer() {
  await Promise.all([
    authProducer.init(),
    usersProducer.init(),
    usersProducerCqrs.init(),
    gerentesProducer.init(),
    gerentesProducerCqrs.init(),
    contasProducer.init(),
    contasProducerCqrs.init(),
    sagaProducer.init(),
    emailProducer.init(),
  ]);

  app.listen(port, () => {
    console.log(`[server]: Server is running at http://localhost:${port}`);
  });
}

startServer().catch((error) => {
  console.error("Failed to start api gateway:", error);
  process.exit(1);
});
