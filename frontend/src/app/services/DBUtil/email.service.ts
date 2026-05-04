import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, catchError, of } from 'rxjs';

export interface EmailPayload {
  destinatario: string;
  assunto: string;
  conteudoHtml: string;
}

export interface EmailResponse {
  message: string;
  destinatario: string;
}

@Injectable({
  providedIn: 'root'
})
export class EmailService {
  private apiUrl = 'http://localhost:3000/emails/enviar';

  constructor(private http: HttpClient) { }

  enviarEmail(payload: EmailPayload): Observable<EmailResponse> {
    return this.http.post<EmailResponse>(this.apiUrl, {
      destinatario: payload.destinatario,
      assunto: payload.assunto,
      conteudoHtml: payload.conteudoHtml
    }).pipe(
      catchError(error => {
        console.error('Erro ao enviar email:', error);
        throw error;
      })
    );
  }

  enviarEmailNotificacao(destinatario: string, titulo: string, mensagem: string): Observable<EmailResponse> {
    const conteudoHtml = `
      <html>
        <head>
          <style>
            body { font-family: Arial, sans-serif; }
            .container { max-width: 600px; margin: 0 auto; padding: 20px; }
            .header { background-color: #007bff; color: white; padding: 15px; border-radius: 5px 5px 0 0; }
            .content { background-color: #f8f9fa; padding: 20px; border-radius: 0 0 5px 5px; }
            .footer { font-size: 12px; color: #666; margin-top: 20px; }
          </style>
        </head>
        <body>
          <div class="container">
            <div class="header">
              <h2>${titulo}</h2>
            </div>
            <div class="content">
              <p>${mensagem}</p>
            </div>
            <div class="footer">
              <p>Este é um email automático, por favor não responda.</p>
            </div>
          </div>
        </body>
      </html>
    `;

    return this.enviarEmail({
      destinatario,
      assunto: titulo,
      conteudoHtml
    });
  }
}
