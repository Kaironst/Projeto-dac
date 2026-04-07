import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup } from '@angular/forms';
import { NgxMaskDirective, provideNgxMask } from 'ngx-mask';

interface Transacao {
  tipo: string;
  valor: number;
  descricao: string;
  data: Date;
}

@Component({
  selector: 'app-cliente-tela',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule, NgxMaskDirective],
  templateUrl: './cliente_tela.html',
  styleUrls: ['./cliente_tela.css']
})
export class ClienteTela {

  perfilForm: FormGroup;

  numConta: string = '';
  cpf: string = '12912861012';
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

  constructor(private fb: FormBuilder) {
    this.perfilForm = this.fb.group({
      nome: [''],
      email: [''],
      telefone: [''],
      salario: [0]
    });

    this.carregarDadosCliente();
  }

  carregarDadosCliente() {

    const clientes = JSON.parse(localStorage.getItem('clientes') || '[]');
    const contas = JSON.parse(localStorage.getItem('contas') || '[]');
    const transacoes = JSON.parse(localStorage.getItem('transacoes') || '[]');

    const cliente = clientes.find((c: any) => c.cpf === this.cpf);
    if (!cliente) return;

    this.perfilForm.patchValue({
      nome: cliente.nome,
      email: cliente.email,
      telefone: cliente.telefone || '',
      salario: cliente.salario
    });

    const conta = contas.find((c: any) => c.clienteCpf === this.cpf);

    if (conta) {
      this.numConta = conta.numero;
      this.saldo = conta.saldo;
      this.limite = conta.limite;
      this.gerente = conta.gerente;
    }

    this.extrato = transacoes
      .filter((t: any) => t.clienteOrigem === this.cpf)
      .map((t: any) => {
        let descricao = '';

        if (t.tipo === 'Transferência') {
          const contaDestino = contas.find((c: any) => c.clienteCpf === t.clienteDestino);
          const clienteDestino = clientes.find((c: any) => c.cpf === t.clienteDestino);

          descricao = clienteDestino
            ? `Para ${clienteDestino.nome} (${contaDestino?.numero})`
            : 'Transferência';
        } else if (t.tipo === 'Depósito') {
          descricao = 'Depósito em conta';
        } else {
          descricao = 'Saque realizado';
        }

        return {
          tipo: t.tipo,
          valor: t.tipo === 'Saque' ? -Math.abs(t.valor) : t.valor,
          descricao,
          data: new Date(t.data)
        };
      })
    .sort((a: Transacao, b: Transacao) => b.data.getTime() - a.data.getTime());

    this.extratoFiltrado = this.extrato;
  }

  depositar() {
    if (this.valorDeposito <= 0) return;

    this.saldo += this.valorDeposito;

    const transacoes = JSON.parse(localStorage.getItem('transacoes') || '[]');

    transacoes.push({
      tipo: 'Depósito',
      valor: this.valorDeposito,
      clienteOrigem: this.cpf,
      clienteDestino: null,
      data: new Date()
    });

    localStorage.setItem('transacoes', JSON.stringify(transacoes));

    this.atualizarConta();
    this.carregarDadosCliente();

    this.valorDeposito = 0;
  }

  sacar() {
    if (this.valorSaque <= 0) {
      alert('Informe um valor válido!');
      return;
    }

    if (this.valorSaque > this.saldo) {
      alert('Saldo insuficiente!');
      return;
    }

    this.saldo -= this.valorSaque;

    const transacoes = JSON.parse(localStorage.getItem('transacoes') || '[]');

    transacoes.push({
      tipo: 'Saque',
      valor: -this.valorSaque,
      clienteOrigem: this.cpf,
      clienteDestino: null,
      data: new Date()
    });

    localStorage.setItem('transacoes', JSON.stringify(transacoes));

    this.atualizarConta();
    this.carregarDadosCliente();

    this.valorSaque = 0;
  }

  transferir() {
    if (this.valorTransferencia <= 0) {
      alert('Informe um valor válido para transferência!');
      return;
    }

  if (!this.contaDestino) {
    alert('Informe a conta de destino!');
    return;
  }

  const contas = JSON.parse(localStorage.getItem('contas') || '[]');
  const clientes = JSON.parse(localStorage.getItem('clientes') || '[]');

  const contaDestino = contas.find((c: any) => c.numero === this.contaDestino);

  if (!contaDestino) {
    alert('Conta de destino não encontrada!');
    return;
  }

  const clienteDestino = clientes.find((c: any) => c.cpf === contaDestino.clienteCpf);

  if (!clienteDestino) {
    alert('Cliente de destino não encontrado!');
    return;
  }

    if (this.valorTransferencia > this.saldo) {
      alert('Saldo insuficiente para transferência!');
      return;
    }

  this.saldo -= this.valorTransferencia;

  contaDestino.saldo += this.valorTransferencia;

  localStorage.setItem('contas', JSON.stringify(contas));

  this.extrato.unshift({
    tipo: 'Transferência',
    valor: -this.valorTransferencia,
    descricao: `Para ${clienteDestino.nome} (${contaDestino.numero})`,
    data: new Date()
  });

  const transacoes = JSON.parse(localStorage.getItem('transacoes') || '[]');

  transacoes.push({
    tipo: 'Transferência',
    valor: this.valorTransferencia,
    clienteOrigem: this.cpf,
    clienteDestino: clienteDestino.cpf,
    descricao: `De ${this.perfilForm.value.nome} (${this.numConta})`,
    data: new Date()
  });

  localStorage.setItem('transacoes', JSON.stringify(transacoes));

  this.valorTransferencia = 0;
  this.contaDestino = '';

  this.atualizarFiltro();
}

  atualizarConta() {
    const contas = JSON.parse(localStorage.getItem('contas') || '[]');

    const conta = contas.find((c: any) => c.clienteCpf === this.cpf);
    if (conta) {
      conta.saldo = this.saldo;
    }

    localStorage.setItem('contas', JSON.stringify(contas));
  }

  calcularLimite(salario: number): number {
    return salario * 0.5;
  }

  atualizarPerfil() {
    const { nome, email, telefone, salario } = this.perfilForm.value;

    if (salario <= 0) {
      alert('Salário inválido!');
      return;
    }

    const clientes = JSON.parse(localStorage.getItem('clientes') || '[]');

    const cliente = clientes.find((c: any) => c.cpf === this.cpf);
    if (cliente) {
      cliente.nome = nome;
      cliente.email = email;
      cliente.telefone = telefone;
      cliente.salario = salario;
    }

    localStorage.setItem('clientes', JSON.stringify(clientes));

    this.limite = this.calcularLimite(salario);

    alert('Perfil atualizado com sucesso!');
  }

  filtrarExtrato() {
  if (!this.dataInicio && !this.dataFim) {
    this.extratoFiltrado = this.extrato;
    return;
  }

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