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

  private readonly consultasUrl = 'http://localhost:8080/consultas/clientes/cpf';
  private readonly contasUrl = 'http://localhost:8080/contas';
  private readonly changeDetectorRef = inject(ChangeDetectorRef);

  cpf: string = '12912861012'; // hardcoded para teste no front original
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

  private mapTipo(tipoId: number, valor: number): { tipo: string, desc: string, val: number } {
    switch (tipoId) {
      case 0: return { tipo: 'Depósito', desc: 'Depósito em conta', val: Math.abs(valor) };
      case 1: return { tipo: 'Saque', desc: 'Saque realizado', val: -Math.abs(valor) };
      case 2: return { tipo: 'Transferência', desc: 'Transferência realizada', val: -Math.abs(valor) }; // Simplificação
      default: return { tipo: 'Outros', desc: 'Operação', val: valor };
    }
  }

  private async buscarClienteNaApi(): Promise<any> {
    try {
      const response = await fetch(`${this.consultasUrl}/${this.cpf}`);
      if (!response.ok) return null;
      return await response.json();
    } catch {
      return null;
    }
  }

  async carregarDadosCliente() {
    const consulta = await this.buscarClienteNaApi();
    if (!consulta) return;

    this.clienteAtual = {
      id: consulta.id,
      cpf: consulta.cpf,
      nome: consulta.nome,
      email: consulta.email,
      telefone: consulta.telefone,
      salario: consulta.salario
    };

    if (consulta.conta) {
      this.numConta = consulta.conta.numero;
      this.saldo = consulta.conta.saldo;
      this.limite = consulta.conta.limite;
      this.gerente = consulta.conta.gerenteId ? `ID: ${consulta.conta.gerenteId}` : 'Não atribuído';
      
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
  }

  async depositar() {
    if (this.valorDeposito <= 0) return;

    try {
      const response = await fetch(`${this.contasUrl}/depositar`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ numero: this.numConta, valor: this.valorDeposito })
      });

      if (response.ok) {
        this.valorDeposito = 0;
        await this.carregarDadosCliente();
        alert('Depósito realizado com sucesso!');
      } else {
        alert('Erro ao depositar.');
      }
    } catch (e) {
      alert('Falha na comunicação com o servidor.');
    }
  }

  async sacar() {
    if (this.valorSaque <= 0) {
      alert('Valor inválido!');
      return;
    }

    if (this.valorSaque > (this.saldo + this.limite)) {
      alert('Saldo insuficiente!');
      return;
    }

    try {
      const response = await fetch(`${this.contasUrl}/sacar`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ numero: this.numConta, valor: this.valorSaque })
      });

      if (response.ok) {
        this.valorSaque = 0;
        await this.carregarDadosCliente();
        alert('Saque realizado com sucesso!');
      } else {
        alert('Erro ao sacar. Verifique o saldo.');
      }
    } catch (e) {
      alert('Falha na comunicação com o servidor.');
    }
  }

  async transferir() {
    if (this.valorTransferencia <= 0 || !this.contaDestino) {
      alert('Dados inválidos!');
      return;
    }

    if (this.valorTransferencia > (this.saldo + this.limite)) {
      alert('Saldo insuficiente!');
      return;
    }

    try {
      const response = await fetch(`${this.contasUrl}/transferir`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ numeroOrigem: this.numConta, numeroDestino: this.contaDestino, valor: this.valorTransferencia })
      });

      if (response.ok) {
        this.valorTransferencia = 0;
        this.contaDestino = '';
        await this.carregarDadosCliente();
        alert('Transferência realizada com sucesso!');
      } else {
        alert('Erro ao transferir. Verifique os dados e tente novamente.');
      }
    } catch (e) {
      alert('Falha na comunicação com o servidor.');
    }
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