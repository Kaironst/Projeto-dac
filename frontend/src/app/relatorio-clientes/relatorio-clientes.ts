import { Component, OnInit, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ConsultaClienteUtil } from '../services/DBUtil/consulta-cliente-util';
import { GerenteUtil } from '../services/DBUtil/gerente-util';

interface ClienteRelatorio {
  cpf: string;
  nome: string;
  email: string;
  salario: number;
  contaNumero: string;
  saldo: number;
  limite: number;
  gerenteCpf: string;
  gerenteNome: string;
}

@Component({
  selector: 'app-relatorio-clientes',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './relatorio-clientes.html',
  styleUrl: './relatorio-clientes.css'
})
export class RelatorioClientes implements OnInit {
  private consultaUtil = inject(ConsultaClienteUtil);
  private gerenteUtil = inject(GerenteUtil);
  private cdr = inject(ChangeDetectorRef);

  public clientes: ClienteRelatorio[] = [];
  public carregando = true;
  public erro = '';

  ngOnInit(): void {
    this.carregarDados();
  }

  carregarDados(): void {
    this.gerenteUtil.getAll().subscribe({
      next: (gerentes) => {
        this.consultaUtil.getAll().subscribe({
          next: (clientesConsulta) => {
            
            try {
              const mapGerentes = new Map(gerentes.map((g: any) => [g.id, g]));

              this.clientes = clientesConsulta.map((c: any) => {
                const g = (c.conta && c.conta.gerenteId) ? mapGerentes.get(c.conta.gerenteId) : null;
                
                return {
                  cpf: c.cpf || '',
                  nome: c.nome || '',
                  email: c.email || '',
                  salario: c.salario || 0,
                  contaNumero: c.conta?.numero || 'S/N',
                  saldo: c.conta?.saldo || 0,
                  limite: c.conta?.limite || 0,
                  gerenteCpf: g?.cpf || 'N/A',
                  gerenteNome: g?.nome || 'Não atribuído'
                };
              });

              this.clientes.sort((a, b) => a.nome.localeCompare(b.nome));
              this.carregando = false;
            } catch (e: any) {
              console.error("Erro interno no processamento:", e);
              this.erro = 'Erro interno ao processar dados: ' + e.message;
              this.carregando = false;
            }
            this.cdr.detectChanges();
          },
          error: (err) => {
            console.error("Erro ao requerer clientes:", err);
            this.erro = 'Erro ao carregar clientes: ' + (err.message || '');
            this.carregando = false;
            this.cdr.detectChanges();
          }
        });
      },
      error: (err) => {
        console.error("Erro ao requerer gerentes:", err);
        this.erro = 'Erro ao carregar gerentes: ' + (err.message || '');
        this.carregando = false;
        this.cdr.detectChanges();
      }
    });
  }
}
