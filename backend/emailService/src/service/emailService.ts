import nodemailer from 'nodemailer';

interface EmailPayload {
  destinatario: string;
  assunto: string;
  conteudoHtml: string;
}

const transporter = nodemailer.createTransport({
  service: 'gmail',
  auth: {
    user: 'manutencaoequipamentos3@gmail.com',
    pass: process.env.EMAIL_PASSWORD
  }
});

export async function sendEmail({
  destinatario,
  assunto,
  conteudoHtml
}: EmailPayload): Promise<void> {
  await transporter.sendMail({
    from: 'manutencaoequipamentos3@gmail.com',
    to: destinatario,
    subject: assunto,
    html: conteudoHtml
  });

  console.log(`E-mail enviado para ${destinatario}`);
}