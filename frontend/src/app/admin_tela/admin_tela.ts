import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';

interface Cliente {
  nome: string;
  saldo: number;
}

interface Gerente {
  nome: string;
  clientes: Cliente[];
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
export class AdminTela {

  gerentes: Gerente[] = [];
  resumoGerentes: GerenteResumo[] = [];

  constructor() {
    this.carregarDados();
    this.processarDados();
  }

  carregarDados() {

    const usuarios = JSON.parse(localStorage.getItem('usuarios') || '[]');
    const clientes = JSON.parse(localStorage.getItem('clientes') || '[]');
    const contas = JSON.parse(localStorage.getItem('contas') || '[]');

    const gerentesUsuarios = usuarios.filter((u: any) => u.tipo === 'gerente');

    this.gerentes = gerentesUsuarios.map((g: any) => {

      const contasDoGerente = contas.filter((c: any) => c.gerente === g.nome);

      const clientesDoGerente: Cliente[] = contasDoGerente.map((conta: any) => {
        const cliente = clientes.find((c: any) => c.cpf === conta.clienteCpf);

        return {
          nome: cliente ? cliente.nome : 'Desconhecido',
          saldo: conta.saldo
        };
      });

      return {
        nome: g.nome,
        clientes: clientesDoGerente
      };
    });
  }

  processarDados() {
    this.resumoGerentes = this.gerentes.map(g => {
      const totalPositivo = g.clientes
        .filter(c => c.saldo >= 0)
        .reduce((sum, c) => sum + c.saldo, 0);

      const totalNegativo = g.clientes
        .filter(c => c.saldo < 0)
        .reduce((sum, c) => sum + c.saldo, 0);

      return {
        nome: g.nome,
        totalClientes: g.clientes.length,
        totalPositivo,
        totalNegativo
      };
    });

    // Mostrar ordem por saldo
    this.resumoGerentes.sort((a, b) => b.totalPositivo - a.totalPositivo);
  }
}