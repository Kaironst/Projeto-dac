import { Request, Response } from 'express';
import { sendEmail } from '../service/emailService';

export async function handleSendEmail(req: Request, res: Response) {
  try {
    const { destinatario, assunto, conteudoHtml } = req.body;

    await sendEmail({ destinatario, assunto, conteudoHtml });

    res.json({ success: true });
  } catch (error) {
    console.error(error);
    res.status(500).json({ error: 'Erro ao enviar e-mail' });
  }
}