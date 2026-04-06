import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

interface Pedido {
  cpf: string;
  nome: string;
  salario: number;
}

@Component({
  selector: 'app-gerente-tela',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './gerente_tela.html',
  styleUrls: ['./gerente_tela.css']
})
export class GerenteTela {

  pedidos: Pedido[] = [
    { cpf: '123.456.789-00', nome: 'João Silva', salario: 2500 },
    { cpf: '987.654.321-00', nome: 'Maria Souza', salario: 1800 }
  ];

  motivoRejeicao: { [cpf: string]: string } = {};

  aprovar(p: Pedido) {

    //fazer isso dum jeito decente depois quando tiver Gerente no back e também um service de email.
    const numeroConta = Math.floor(1000 + Math.random() * 9000).toString();
    const senha = Math.random().toString(36).slice(-6);

    const limite = p.salario >= 2000 ? p.salario / 2 : 0;

    console.log('Cliente aprovado:', {
      ...p,
      numeroConta,
      senha,
      limite,
      dataAprovacao: new Date()
    });

    this.removerPedido(p);
  }

  recusar(p: Pedido) {
    const motivo = this.motivoRejeicao[p.cpf];

    if (!motivo) {
      alert('Informe o motivo da recusa');
      return;
    }

    console.log('Cliente recusado:', {
      ...p,
      motivo,
      dataRejeicao: new Date()
    });

    this.removerPedido(p);
  }

  removerPedido(p: Pedido) {
    this.pedidos = this.pedidos.filter(x => x !== p);
  }
}