import { Router, Request, Response } from "express";
import axios from "axios";

const router = Router();

const emailServiceUrl = process.env.EMAIL_SERVICE_URL || "http://email-service:3005";
router.post("/enviar", async (req: Request, res: Response) => {
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

    return res.status(200).json({
      message: "Email enviado com sucesso",
      destinatario
    });

  } catch (error: any) {
    console.error("Erro ao enviar email:", error);

    return res.status(500).json({
      error: "Erro ao enviar email",
      details: error.message
    });
  }
});

export default router;