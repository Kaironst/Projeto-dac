import express from 'express';
import { Express, Request, Response } from "express";
import cors from "cors";
import axios from "axios";
import ClienteController from "./controller/ClienteController";
import GerenteController from "./controller/GerenteController";
import { GerentesProducer, usersProducer } from './messaging/GenericProducerRPC';

const app: Express = express();
const port = process.env.PORT || 3000;
const emailServiceUrl = process.env.EMAIL_SERVICE_URL || "http://email-service:3005";
export const rabbitmqUrl = "amqp://usuario:admin@rabbitmq";

//passei um tempo enorme tentando entender por que o orquestrator tava mandando como se a menssagem fosse null
//era por causa dessa linha aq
app.use(express.json());
app.use(express.urlencoded({ extended: true }));
app.use(cors());

app.use("/clientes", ClienteController);

// Email endpoint proxy
app.post("/emails/enviar", async (req: Request, res: Response) => {
  try {
    const { destinatario, assunto, conteudoHtml } = req.body;
    
    if (!destinatario || !assunto || !conteudoHtml) {
      return res.status(400).json({ 
        error: "Missing required fields: destinatario, assunto, conteudoHtml" 
      });
    }

    await axios.post(`${emailServiceUrl}/send-email`, {
      destinatario,
      assunto,
      conteudoHtml
    });

    res.status(200).json({ 
      message: "Email enviado com sucesso",
      destinatario 
    });
  } catch (error: any) {
    console.error("Erro ao enviar email:", error.message);
    res.status(500).json({ 
      error: "Erro ao enviar email",
      details: error.message 
    });
  }
});

app.use("/gerentes", GerenteController);
usersProducer.init();
GerentesProducer.init();

app.listen(port, () => {
  console.log(`[server]: Server is running at http://localhost:${port}`);
});
