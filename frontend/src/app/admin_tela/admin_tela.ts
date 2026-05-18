import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { forkJoin } from 'rxjs';
import { Cliente, ClienteUtil } from '../services/DBUtil/cliente-util';
import { Conta, ContaUtil } from '../services/DBUtil/conta-util';
import { Gerente, GerenteUtil } from '../services/DBUtil/gerente-util';

interface ClienteResumo {
  nome: string;
  saldo: number;
}

interface GerenteResumo {
  nome: string;
  totalClientes: number;
  totalPositivo: number;
  totalNegativo: number;
}

@Component({
  selector: 'app-admin-tela',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './admin_tela.html',
  styleUrls: ['./admin_tela.css']
})
export class AdminTela implements OnInit {

  private clienteUtil = inject(ClienteUtil);
  private contaUtil = inject(ContaUtil);
  private gerenteUtil = inject(GerenteUtil);

  resumoGerentes: GerenteResumo[] = [];

  ngOnInit(): void {
    this.carregarDados();
  }

  carregarDados(): void {
    forkJoin({
      gerentes: this.gerenteUtil.getAll(),
      clientes: this.clienteUtil.getAll(),
      contas: this.contaUtil.getAll()
    }).subscribe({
      next: ({ gerentes, clientes, contas }) => {
        this.resumoGerentes = this.processarDados(gerentes, clientes, contas);
      },
      error: (erro) => {
        console.error('Erro ao carregar dados administrativos:', erro);
        this.resumoGerentes = [];
      }
    });
  }

  private processarDados(gerentes: Gerente[], clientes: Cliente[], contas: Conta[]): GerenteResumo[] {
    return gerentes
      .filter(gerente => !gerente.administrador)
      .map(gerente => {
        const clientesDoGerente = this.clientesDoGerente(gerente, clientes, contas);
        const totalPositivo = clientesDoGerente
          .filter(cliente => cliente.saldo >= 0)
          .reduce((sum, cliente) => sum + cliente.saldo, 0);

        const totalNegativo = clientesDoGerente
          .filter(cliente => cliente.saldo < 0)
          .reduce((sum, cliente) => sum + cliente.saldo, 0);

        return {
          nome: gerente.nome ?? 'Gerente sem nome',
          totalClientes: clientesDoGerente.length,
          totalPositivo,
          totalNegativo
        };
      })
      .sort((a, b) => b.totalPositivo - a.totalPositivo);
  }

  private clientesDoGerente(gerente: Gerente, clientes: Cliente[], contas: Conta[]): ClienteResumo[] {
    return contas
      .filter(conta => conta.gerente?.id === gerente.id)
      .map(conta => {
        const cliente = clientes.find(item => item.id === conta.cliente?.id);
        return {
          nome: cliente?.nome ?? 'Desconhecido',
          saldo: conta.saldo ?? 0
        };
      });
  }
}
