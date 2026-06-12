import { Component, OnInit, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ConsultaClienteUtil } from '../services/DBUtil/consulta-cliente-util';
import { GerenteUtil } from '../services/DBUtil/gerente-util';

interface Cliente {
  nome: string;
  saldo: number;
}

interface Gerente {
  id: number;
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
export class AdminTela implements OnInit {

  private consultaUtil = inject(ConsultaClienteUtil);
  private gerenteUtil = inject(GerenteUtil);
  private cdr = inject(ChangeDetectorRef);

  gerentes: Gerente[] = [];
  resumoGerentes: GerenteResumo[] = [];
  carregando = true;
  erro = '';

  ngOnInit(): void {
    this.carregarDados();
  }

  carregarDados() {
    this.carregando = true;

    this.gerenteUtil.getAll().subscribe({
      next: (gerentesList: any[]) => {
        // Obter apenas gerentes de verdade, que não são administradores globais
        const gerentesUsuarios = gerentesList.filter((g: any) => !g.administrador);

        this.consultaUtil.getAll().subscribe({
          next: (clientesList: any[]) => {
            
            this.gerentes = gerentesUsuarios.map((g: any) => {
              // Buscar todos os clientes cuja conta tem o gerenteId igual a g.id
              const contasDoGerente = clientesList.filter((c: any) => c.conta && c.conta.gerenteId === g.id);

              const clientesDoGerente: Cliente[] = contasDoGerente.map((c: any) => ({
                nome: c.nome || 'Desconhecido',
                saldo: c.conta?.saldo || 0
              }));

              return {
                id: g.id,
                nome: g.nome,
                clientes: clientesDoGerente
              };
            });

            this.processarDados();
            this.carregando = false;
            this.cdr.detectChanges();
          },
          error: (err) => {
            this.erro = 'Erro ao carregar dados de clientes.';
            this.carregando = false;
            this.cdr.detectChanges();
          }
        });
      },
      error: (err) => {
        this.erro = 'Erro ao carregar dados de gerentes.';
        this.carregando = false;
        this.cdr.detectChanges();
      }
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