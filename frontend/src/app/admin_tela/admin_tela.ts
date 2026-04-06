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
    this.gerentes = [
      {
        nome: 'Gerente A',
        clientes: [
          { nome: 'Cliente 1', saldo: 1000 },
          { nome: 'Cliente 2', saldo: -200 },
          { nome: 'Cliente 3', saldo: 0 }
        ]
      },
      {
        nome: 'Gerente B',
        clientes: [
          { nome: 'Cliente 4', saldo: 5000 },
          { nome: 'Cliente 5', saldo: -1000 }
        ]
      },
      {
        nome: 'Gerente C',
        clientes: [
          { nome: 'Cliente 6', saldo: -300 },
          { nome: 'Cliente 7', saldo: -200 }
        ]
      },
      {
        nome: 'Gerente D',
        clientes: [
          { nome: 'Cliente 8', saldo: -300 },
          { nome: 'Cliente 9', saldo: -200 }
        ]
      }
    ];
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