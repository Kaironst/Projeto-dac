import { Router, Request, Response } from "express";
import { emailProducer } from "../messaging/GenericProducerRPC";

const router = Router();

router.post("/enviar", async (req: Request, res: Response) => {
  try {

    const { destinatario, assunto, conteudoHtml } = req.body;

    if (!destinatario || !assunto || !conteudoHtml) {
      return res.status(400).json({
        error: "Missing required fields"
      });
    }

    const response = await emailProducer.requestService({
      operation: "SEND",
      dataType: "email",
      data: [{
        destinatario,
        assunto,
        conteudoHtml
      }]
    });

    return res.status(200).json(response);

  } catch (error: any) {

    console.error(error);

    return res.status(500).json({
      error: error.message
    });

  }
});

export default router;