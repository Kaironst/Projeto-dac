import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatCardModule } from '@angular/material/card';
import { HttpClient } from '@angular/common/http';
import { EmailService } from '../services/DBUtil/email.service';

interface PedidoAprovacao {
  cpf: string;
  nome: string;
  salario: number;
  email: string;
  telefone: string;
}

@Component({
  selector: 'app-gerente-tela',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatTableModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatCardModule
  ],
  templateUrl: './gerente_tela.html',
  styleUrls: ['./gerente_tela.css']
})
export class GerenteTela implements OnInit {
  private http = inject(HttpClient);
  private emailService = inject(EmailService);

  pedidos: PedidoAprovacao[] = [];
  motivoRejeicao: { [cpf: string]: string } = {};
  carregando = false;
  mensagem = '';

  ngOnInit() {
    this.carregarPedidos();
  }

  carregarPedidos() {
    this.carregando = true;
    // Chamar endpoint GET para buscar pedidos pendentes de aprovação
    this.http.get<PedidoAprovacao[]>('http://localhost:8080/gerentes/pedidos-aprovacao')
      .subscribe({
        next: (dados) => {
          this.pedidos = dados;
          this.carregando = false;
        },
        error: (erro) => {
          console.error('Erro ao carregar pedidos:', erro);
          this.carregando = false;
        }
      });
  }

  aprovar(pedido: PedidoAprovacao) {
    this.carregando = true;
    const numeroConta = Math.floor(1000 + Math.random() * 9000).toString();
    const senhaAleatoria = this.gerarSenhaAleatoria();

    const payload = {
      cpf: pedido.cpf,
      nome: pedido.nome,
      email: pedido.email,
      telefone: pedido.telefone,
      salario: pedido.salario,
      numeroConta: numeroConta,
      senha: senhaAleatoria
    };

    // Chamar endpoint POST para aprovar cliente
    this.http.post('http://localhost:8080/gerentes/aprovar-cliente', payload)
      .subscribe({
        next: () => {
          this.enviarEmailAproacao(pedido, numeroConta, senhaAleatoria);
          this.mensagem = `Cliente ${pedido.nome} aprovado com sucesso!`;
          this.carregarPedidos();
        },
        error: (erro) => {
          console.error('Erro ao aprovar cliente:', erro);
          this.mensagem = `Erro ao aprovar cliente: ${erro.error?.message || 'Erro desconhecido'}`;
          this.carregando = false;
        }
      });
  }

  rejeitar(pedido: PedidoAprovacao) {
    const motivo = this.motivoRejeicao[pedido.cpf];

    if (!motivo || motivo.trim() === '') {
      this.mensagem = 'Informe o motivo da rejeição';
      return;
    }

    this.carregando = true;

    const payload = {
      cpf: pedido.cpf,
      motivo: motivo
    };

    // Chamar endpoint POST para rejeitar cliente
    this.http.post('http://localhost:8080/gerentes/rejeitar-cliente', payload)
      .subscribe({
        next: () => {
          this.enviarEmailRejeicao(pedido, motivo);
          this.mensagem = `Cliente ${pedido.nome} rejeitado com sucesso!`;
          this.motivoRejeicao[pedido.cpf] = '';
          this.carregarPedidos();
        },
        error: (erro) => {
          console.error('Erro ao rejeitar cliente:', erro);
          this.mensagem = `Erro ao rejeitar cliente: ${erro.error?.message || 'Erro desconhecido'}`;
          this.carregando = false;
        }
      });
  }

  private enviarEmailAproacao(pedido: PedidoAprovacao, numeroConta: string, senha: string) {
    const conteudo = `
      <h2>Parabéns! Sua conta foi aprovada!</h2>
      <p>Bem-vindo ao BANTADS - Internet Banking do TADS</p>
      
      <h3>Dados da sua conta:</h3>
      <ul>
        <li><strong>Nome:</strong> ${pedido.nome}</li>
        <li><strong>Número da Conta:</strong> ${numeroConta}</li>
        <li><strong>Seu e-mail:</strong> ${pedido.email}</li>
      </ul>

      <h3>Credenciais de acesso:</h3>
      <p><strong>Usuário:</strong> ${pedido.email}</p>
      <p><strong>Senha:</strong> ${senha}</p>

      <p style="color: red;"><strong>⚠️ IMPORTANTE:</strong> Altere sua senha no primeiro acesso!</p>
      
      <p>Acesse o sistema em: <a href="http://localhost:4200">http://localhost:4200</a></p>
      
      <p>Qualquer dúvida, entre em contato com seu gerente responsável.</p>
    `;

    this.emailService.enviarEmail({
      destinatario: pedido.email,
      assunto: 'Sua conta BANTADS foi aprovada!',
      conteudoHtml: conteudo
    }).subscribe({
      next: () => {
        console.log('Email de aprovação enviado com sucesso');
      },
      error: (erro) => {
        console.error('Erro ao enviar email de aprovação:', erro);
      }
    });
  }

  private enviarEmailRejeicao(pedido: PedidoAprovacao, motivo: string) {
    const conteudo = `
      <h2>Sua solicitação de cadastro foi recusada</h2>
      
      <p>Prezado(a) ${pedido.nome},</p>
      
      <p>Lamentavelmente, sua solicitação de cadastro no BANTADS foi analisada e <strong>recusada</strong>.</p>
      
      <h3>Motivo da recusa:</h3>
      <p>${motivo}</p>
      
      <p>Se acredita que houve um erro, ou se deseja mais informações, por favor entre em contato conosco.</p>
      
      <p>Atenciosamente,<br>Equipe BANTADS</p>
    `;

    this.emailService.enviarEmail({
      destinatario: pedido.email,
      assunto: 'Resultado da sua solicitação de cadastro BANTADS',
      conteudoHtml: conteudo
    }).subscribe({
      next: () => {
        console.log('Email de rejeição enviado com sucesso');
      },
      error: (erro) => {
        console.error('Erro ao enviar email de rejeição:', erro);
      }
    });
  }

  private gerarSenhaAleatoria(): string {
    return Math.random().toString(36).slice(-8);
  }

  limparMensagem() {
    this.mensagem = '';
  }
}