import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

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

  saldo: number = 0;

  valorDeposito: number = 0;
  valorSaque: number = 0;
  valorTransferencia: number = 0;
  contaDestino: string = '';

  extrato: Transacao[] = [];

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
    if (this.valorSaque > 0) {
      this.saldo -= this.valorSaque;

      this.extrato.unshift({
        tipo: 'Saque',
        valor: this.valorSaque,
        descricao: 'Saque realizado',
        data: new Date()
      });

      this.valorSaque = 0;
    }
  }

  transferir() {
    if (this.valorTransferencia > 0 && this.contaDestino) {
      this.saldo -= this.valorTransferencia;

      this.extrato.unshift({
        tipo: 'Transferência',
        valor: this.valorTransferencia,
        descricao: `Para conta ${this.contaDestino}`,
        data: new Date()
      });

      this.valorTransferencia = 0;
      this.contaDestino = '';
    }
  }
}