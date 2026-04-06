import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

interface Pedido {
  cpf: string;
  nome: string;
  salario: number;
  email: string;
  telefone: string;
  endereco: any;
}

@Component({
  selector: 'app-gerente-tela',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './gerente_tela.html',
  styleUrls: ['./gerente_tela.css']
})
export class GerenteTela {

  pedidos: Pedido[] = [];
  motivoRejeicao: { [cpf: string]: string } = {};

  constructor() {
    this.carregarPedidos();
  }

  carregarPedidos() {
    const pedidos = JSON.parse(localStorage.getItem('pedidosCadastro') || '[]');
    this.pedidos = pedidos;
  }

  aprovar(p: Pedido) {

    const clientes = JSON.parse(localStorage.getItem('clientes') || '[]');
    const contas = JSON.parse(localStorage.getItem('contas') || '[]');
    const usuarios = JSON.parse(localStorage.getItem('usuarios') || '[]');
    const pedidos = JSON.parse(localStorage.getItem('pedidosCadastro') || '[]');

    const numeroConta = Math.floor(1000 + Math.random() * 9000).toString();
    const senha = Math.random().toString(36).slice(-6);
    const limite = p.salario >= 2000 ? p.salario / 2 : 0;

    const novoCliente = {
      cpf: p.cpf,
      nome: p.nome,
      email: p.email,
      telefone: p.telefone,
      salario: p.salario,
      endereco: p.endereco
    };

    clientes.push(novoCliente);

    const novaConta = {
      clienteCpf: p.cpf,
      numero: numeroConta,
      saldo: 0,
      limite: limite,
      gerente: 'Geniéve',
      dataCriacao: new Date()
    };

    contas.push(novaConta);

    usuarios.push({
      cpf: p.cpf,
      nome: p.nome,
      email: p.email,
      senha: senha,
      tipo: 'cliente'
    });

    const novosPedidos = pedidos.filter((x: Pedido) => x.cpf !== p.cpf);

    localStorage.setItem('clientes', JSON.stringify(clientes));
    localStorage.setItem('contas', JSON.stringify(contas));
    localStorage.setItem('usuarios', JSON.stringify(usuarios));
    localStorage.setItem('pedidosCadastro', JSON.stringify(novosPedidos));

    console.log('Cliente aprovado:', {
      ...p,
      numeroConta,
      senha,
      limite,
      dataAprovacao: new Date()
    });

    this.carregarPedidos();
  }

  recusar(p: Pedido) {

    const motivo = this.motivoRejeicao[p.cpf];

    if (!motivo) {
      alert('Informe o motivo da recusa');
      return;
    }

    const pedidos = JSON.parse(localStorage.getItem('pedidosCadastro') || '[]');

    const novosPedidos = pedidos.filter((x: Pedido) => x.cpf !== p.cpf);

    const recusados = JSON.parse(localStorage.getItem('pedidosRecusados') || '[]');

    recusados.push({
      ...p,
      motivo,
      dataRejeicao: new Date()
    });

    localStorage.setItem('pedidosCadastro', JSON.stringify(novosPedidos));
    localStorage.setItem('pedidosRecusados', JSON.stringify(recusados));

    console.log('Cliente recusado:', {
      ...p,
      motivo,
      dataRejeicao: new Date()
    });

    this.carregarPedidos();
  }
}