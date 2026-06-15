import { Component, ChangeDetectorRef, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule} from '@angular/forms';
import { Auth } from '../services/auth/auth';
import { HttpClient } from '@angular/common/http';

interface Cliente {
  id?: number;
  cpf: string;
  nome: string;
  email: string;
  telefone: string;
  salario: number;
}

interface Transacao {
  tipo: string;
  valor: number;
  descricao: string;
  data: Date;
}

@Component({
  selector: 'app-cliente-tela',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './cliente_tela.html',
  styleUrls: ['./cliente_tela.css']
})
export class ClienteTela {

  private readonly consultasUrl = 'http://localhost:8080/consultas/clientes/cpf';
  private readonly contasUrl = 'http://localhost:8080/contas';
  private readonly changeDetectorRef = inject(ChangeDetectorRef);
  private readonly auth = inject(Auth);
  private http = inject(HttpClient); 

  cpf: string = '';
  clienteAtual: Cliente | null = null;

  numConta: string = '';
  gerente: string = '';

  saldo: number = 0;
  limite: number = 0;

  valorDeposito = 0;
  valorSaque = 0;
  valorTransferencia = 0;
  contaDestino: any = '';

  extrato: Transacao[] = [];
  extratoFiltrado: Transacao[] = [];

  dataInicio: string = '';
  dataFim: string = '';

  constructor() {
    this.auth.getClienteAtual().subscribe(cliente => {
      if (cliente && cliente.cpf) {
        this.cpf = cliente.cpf;
        this.carregarDadosCliente();
      }
    });
  }

  private mapTipo(tipoId: number, valor: number): { tipo: string, desc: string, val: number } {
    switch (tipoId) {
      case 0: return { tipo: 'Depósito', desc: 'Depósito em conta', val: Math.abs(valor) };
      case 1: return { tipo: 'Saque', desc: 'Saque realizado', val: -Math.abs(valor) };
      case 2: return { tipo: 'Transferência', desc: 'Transferência realizada', val: -Math.abs(valor) }; // Simplificação
      default: return { tipo: 'Outros', desc: 'Operação', val: valor };
    }
  }

  carregarDadosCliente() {
  this.http.get<any>(`${this.consultasUrl}/${this.cpf}`)
    .subscribe({
      next: (consulta) => {
        this.clienteAtual = {
          id: consulta.id,
          cpf: consulta.cpf,
          nome: consulta.nome,
          email: consulta.email,
          telefone: consulta.telefone,
          salario: consulta.salario
        };

        if (consulta.conta) {
          this.numConta = consulta.conta.id;
          this.saldo = consulta.conta.saldo;
          this.limite = consulta.conta.limite;
          this.gerente = consulta.conta.gerenteId
            ? `ID: ${consulta.conta.gerenteId}`
            : 'Não atribuído';

          this.extrato = (consulta.conta.extrato || []).map((h: any) => {
            const info = this.mapTipo(h.tipo, h.valorMovimentacao);

            return {
              tipo: info.tipo,
              valor: info.val,
              descricao: info.desc,
              data: new Date(h.dataHora)
            };
          });
        }

        this.atualizarFiltro();
        this.changeDetectorRef.detectChanges();
      },
      error: (erro) => {
        console.error('Erro ao carregar cliente:', erro);
      }
    });
}

  depositar() {
  if (this.valorDeposito <= 0) {
    return;
  }

  this.http.post(
    `${this.contasUrl}/depositar`,
    {
      numero: this.numConta,
      valor: this.valorDeposito
    }
  ).subscribe({
    next: () => {
      this.valorDeposito = 0;
      this.carregarDadosCliente();
      alert('Depósito realizado com sucesso!');
    },
    error: (erro) => {
      console.error('Erro ao depositar:', erro);
      alert(`Erro ao depositar. ${this.valorDeposito} | ${this.numConta}`);
    }
  });
}

  sacar() {
  if (this.valorSaque <= 0) {
    alert('Valor inválido!');
    return;
  }

  if (this.valorSaque > (this.saldo + this.limite)) {
    alert('Saldo insuficiente!');
    return;
  }

  this.http.post(
    `${this.contasUrl}/sacar`,
    {
      numero: this.numConta,
      valor: this.valorSaque
    }
  ).subscribe({
    next: () => {
      this.valorSaque = 0;
      this.carregarDadosCliente();
      alert('Saque realizado com sucesso!');
    },
    error: (erro) => {
      console.error('Erro ao sacar:', erro);
      alert('Erro ao sacar. Verifique o saldo.');
    }
  });
}

  transferir() {
  if (this.valorTransferencia <= 0 || !this.contaDestino) {
    alert('Dados inválidos!');
    return;
  }

  if (this.valorTransferencia > (this.saldo + this.limite)) {
    alert('Saldo insuficiente!');
    return;
  }

  this.http.post(
    `${this.contasUrl}/transferir`,
    {
      numeroOrigem: this.numConta,
      numeroDestino: this.contaDestino,
      valor: this.valorTransferencia
    }
  ).subscribe({
    next: () => {
      this.valorTransferencia = 0;
      this.contaDestino = '';
      this.carregarDadosCliente();
      alert('Transferência realizada com sucesso!');
    },
    error: (erro) => {
      console.error('Erro ao transferir:', erro);
      alert('Erro ao transferir. Verifique os dados e tente novamente.');
    }
  });
}

  filtrarExtrato() {
    const inicio = this.dataInicio ? new Date(this.dataInicio) : null;
    const fim = this.dataFim ? new Date(this.dataFim) : null;

    this.extratoFiltrado = this.extrato.filter(t => {
      const data = new Date(t.data);

      if (inicio && data < inicio) return false;
      if (fim && data > fim) return false;

      return true;
    });
  }

  atualizarFiltro() {
    this.filtrarExtrato();
  }

  limparFiltro() {
    this.dataInicio = '';
    this.dataFim = '';
    this.extratoFiltrado = this.extrato;
  }
}