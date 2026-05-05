import { Component, ChangeDetectorRef, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule} from '@angular/forms';

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

  private readonly apiUrl = 'http://localhost:8080/clientes';
  private readonly changeDetectorRef = inject(ChangeDetectorRef);

  cpf: string = '12912861012';
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

    this.carregarDadosCliente();
  }


  private async buscarClienteNaApi(): Promise<Cliente | null> {
    try {
      const response = await fetch(this.apiUrl);

      if (!response.ok) return null;

      const clientes = await response.json();

      return clientes.find((c: Cliente) => c.cpf === this.cpf) ?? null;

    } catch {
      return null;
    }
  }

  private async atualizarClienteNaApi(cliente: Cliente): Promise<boolean> {
    if (!cliente.id) return false;

    try {
      const response = await fetch(`${this.apiUrl}/${cliente.id}`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(cliente)
      });

      return response.ok;
    } catch {
      return false;
    }
  }

  async carregarDadosCliente() {
  const cliente = await this.buscarClienteNaApi();

  if (!cliente) return;

  this.clienteAtual = cliente;

  this.limite = this.calcularLimite(cliente.salario);

  // Mock enquanto não integra contas
  this.numConta = '0001';
  this.gerente = 'Gerente Padrão';
  this.saldo = 0;

  this.changeDetectorRef.detectChanges();
}


  depositar() {
    if (this.valorDeposito <= 0) return;

    this.saldo += this.valorDeposito;

    this.extrato.unshift({
      tipo: 'Depósito',
      valor: this.valorDeposito,
      descricao: 'Depósito em conta',
      data: new Date()
    });

    this.valorDeposito = 0;
    this.atualizarFiltro();
  }

  sacar() {
    if (this.valorSaque <= 0) {
      alert('Valor inválido!');
      return;
    }

    if (this.valorSaque > this.saldo) {
      alert('Saldo insuficiente!');
      return;
    }

    this.saldo -= this.valorSaque;

    this.extrato.unshift({
      tipo: 'Saque',
      valor: -this.valorSaque,
      descricao: 'Saque realizado',
      data: new Date()
    });

    this.valorSaque = 0;
    this.atualizarFiltro();
  }

  transferir() {
    if (this.valorTransferencia <= 0 || !this.contaDestino) {
      alert('Dados inválidos!');
      return;
    }

    if (this.valorTransferencia > this.saldo) {
      alert('Saldo insuficiente!');
      return;
    }

    this.saldo -= this.valorTransferencia;

    this.extrato.unshift({
      tipo: 'Transferência',
      valor: -this.valorTransferencia,
      descricao: `Para conta ${this.contaDestino}`,
      data: new Date()
    });

    this.valorTransferencia = 0;
    this.contaDestino = '';

    this.atualizarFiltro();
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

  calcularLimite(salario: number): number {
    return salario * 0.5;
  }
}