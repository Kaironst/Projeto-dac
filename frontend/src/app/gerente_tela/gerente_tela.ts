import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatCardModule } from '@angular/material/card';
import { HttpClient } from '@angular/common/http';

interface PedidoAprovacao {
  id?: number;
  solicitacaoId?: number;
  cpf: string;
  nome: string;
  salario: number;
  email: string;
  telefone: string;
  gerenteId?: number;
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
    this.http.get<PedidoAprovacao[]>('/gerentes/pedidos-aprovacao')
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

    const payload = {
      solicitacaoId: pedido.solicitacaoId ?? pedido.id,
      cpf: pedido.cpf,
      gerenteId: pedido.gerenteId
    };

    // Chamar endpoint POST para aprovar cliente
    this.http.post('/gerentes/aprovar-cliente', payload)
      .subscribe({
        next: () => {
          this.mensagem = `Aprovação de ${pedido.nome} enviada para processamento.`;
          this.removerPedidoDaLista(pedido);
          this.sincronizarPedidosDepoisDaSaga();
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
      solicitacaoId: pedido.solicitacaoId ?? pedido.id,
      gerenteId: pedido.gerenteId ?? null,
      motivo: motivo
    };

    // Chamar endpoint POST para rejeitar cliente
    this.http.post('/gerentes/rejeitar-cliente', payload)
      .subscribe({
        next: () => {
          this.mensagem = `Rejeição de ${pedido.nome} enviada para processamento.`;
          this.motivoRejeicao[pedido.cpf] = '';
          this.removerPedidoDaLista(pedido);
          this.sincronizarPedidosDepoisDaSaga();
        },
        error: (erro) => {
          console.error('Erro ao rejeitar cliente:', erro);
          this.mensagem = `Erro ao rejeitar cliente: ${erro.error?.message || 'Erro desconhecido'}`;
          this.carregando = false;
        }
      });
  }

  private removerPedidoDaLista(pedido: PedidoAprovacao) {
    const solicitacaoId = pedido.solicitacaoId ?? pedido.id;
    this.pedidos = this.pedidos.filter(item => (item.solicitacaoId ?? item.id) !== solicitacaoId);
    this.carregando = false;
  }

  private sincronizarPedidosDepoisDaSaga() {
    window.setTimeout(() => this.carregarPedidos(), 1500);
  }

  limparMensagem() {
    this.mensagem = '';
  }
}
