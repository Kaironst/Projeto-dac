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

  cpf: string = '123.456.789-00';
  gerente: string = 'Carlos Souza';

  saldo: number = -500;
  limite: number = 1000;

  valorDeposito = 0;
  valorSaque = 0;
  valorTransferencia = 0;
  contaDestino: any = '';

  extrato: Transacao[] = [];

  constructor(private fb: FormBuilder) {
    this.perfilForm = this.fb.group({
      nome: ['João Silva'],
      email: ['joaosilva@gmail.com'],
      telefone: ['41998765432'],
      salario: [2000]
    });
  }

  depositar() {
    if (this.valorDeposito > 0) {
      this.saldo += this.valorDeposito;

      this.extrato.unshift({
        tipo: 'Depósito',
        valor: this.valorDeposito,
        descricao: 'Depósito em conta',
        data: new Date()
      });

      this.valorDeposito = 0;
    }
  }

  sacar() {
    if (this.valorSaque <= 0) {
      alert('Informe um valor válido para saque!');
      return;
    }

    if (this.valorSaque > this.saldo) {
      alert('Saldo insuficiente para saque!');
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

    if (this.valorTransferencia > this.saldo) {
      alert('Saldo insuficiente para transferência!');
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

    let novoLimite = this.calcularLimite(salario);

    if (this.saldo < 0 && Math.abs(this.saldo) > novoLimite) {
      this.limite = Math.abs(this.saldo);
    } else {
      this.limite = novoLimite;
    }

    alert(
      `Perfil atualizado!\n\nNome: ${nome}\nEmail: ${email}\nTelefone: ${telefone}\nSalário: R$ ${salario.toFixed(2)}\nLimite: R$ ${this.limite.toFixed(2)}\nSaldo: R$ ${this.saldo.toFixed(2)}\nGerente: ${this.gerente}`
    );
  }
}