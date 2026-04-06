import { Component, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { Navbar } from "./navbar/navbar";

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, Navbar],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {
  protected readonly title = signal('projeto-dac');

  ngOnInit() {
  this.inicializarDados();
  }

  inicializarDados() {

  if (localStorage.getItem('clientes')) return;

  const clientes = [
    {
      cpf: '12912861012',
      nome: 'Catharyna',
      email: 'cli1@bantads.com.br',
      senha: 'tads',
      salario: 10000,
      endereco: 'Rua A, Curitiba'
    },
    {
      cpf: '09506382000',
      nome: 'Cleuddônio',
      email: 'cli2@bantads.com.br',
      senha: 'tads',
      salario: 20000,
      endereco: 'Rua B, Curitiba'
    },
    {
      cpf: '85733854057',
      nome: 'Catianna',
      email: 'cli3@bantads.com.br',
      senha: 'tads',
      salario: 3000,
      endereco: 'Rua C, Curitiba'
    },
    {
      cpf: '58872160006',
      nome: 'Cutardo',
      email: 'cli4@bantads.com.br',
      senha: 'tads',
      salario: 500,
      endereco: 'Rua D, Curitiba'
    },
    {
      cpf: '76179646090',
      nome: 'Coândrya',
      email: 'cli5@bantads.com.br',
      senha: 'tads',
      salario: 1500,
      endereco: 'Rua E, Curitiba'
    }
  ];

  const contas = [
    {
      clienteCpf: '12912861012',
      numero: '1291',
      saldo: 800,
      limite: 5000,
      gerente: 'Geniéve',
      dataCriacao: new Date('2000-01-01')
    },
    {
      clienteCpf: '09506382000',
      numero: '0950',
      saldo: -10000,
      limite: 10000,
      gerente: 'Godophredo',
      dataCriacao: new Date('1990-10-10')
    },
    {
      clienteCpf: '85733854057',
      numero: '8573',
      saldo: -1000,
      limite: 1500,
      gerente: 'Gyândula',
      dataCriacao: new Date('2012-12-12')
    },
    {
      clienteCpf: '58872160006',
      numero: '5887',
      saldo: 150000,
      limite: 0,
      gerente: 'Geniéve',
      dataCriacao: new Date('2022-02-22')
    },
    {
      clienteCpf: '76179646090',
      numero: '7617',
      saldo: 1500,
      limite: 0,
      gerente: 'Godophredo',
      dataCriacao: new Date('2025-01-01')
    }
  ];

  const transacoes = [

  {
    tipo: 'Depósito',
    valor: 1000,
    clienteOrigem: '12912861012',
    clienteDestino: null,
    descricao: 'Depósito em conta',
    data: new Date('2020-01-01T10:00:00')
  },
  {
    tipo: 'Depósito',
    valor: 900,
    clienteOrigem: '12912861012',
    clienteDestino: null,
    descricao: 'Depósito em conta',
    data: new Date('2020-01-01T11:00:00')
  },
  {
    tipo: 'Saque',
    valor: -550,
    clienteOrigem: '12912861012',
    clienteDestino: null,
    descricao: 'Saque realizado',
    data: new Date('2020-01-01T12:00:00')
  },
  {
    tipo: 'Saque',
    valor: -350,
    clienteOrigem: '12912861012',
    clienteDestino: null,
    descricao: 'Saque realizado',
    data: new Date('2020-01-01T13:00:00')
  },
  {
    tipo: 'Depósito',
    valor: 2000,
    clienteOrigem: '12912861012',
    clienteDestino: null,
    descricao: 'Depósito em conta',
    data: new Date('2020-01-10T15:00:00')
  },
  {
    tipo: 'Saque',
    valor: -500,
    clienteOrigem: '12912861012',
    clienteDestino: null,
    descricao: 'Saque realizado',
    data: new Date('2020-01-15T08:00:00')
  },

  {
    tipo: 'Transferência',
    valor: -1700,
    clienteOrigem: '12912861012',
    clienteDestino: '09506382000',
    descricao: 'Para Cleuddônio (0950)',
    data: new Date('2020-01-20T12:00:00')
  },

  {
    tipo: 'Depósito',
    valor: 1000,
    clienteOrigem: '09506382000',
    clienteDestino: null,
    descricao: 'Depósito em conta',
    data: new Date('2025-01-01T12:00:00')
  },
  {
    tipo: 'Depósito',
    valor: 5000,
    clienteOrigem: '09506382000',
    clienteDestino: null,
    descricao: 'Depósito em conta',
    data: new Date('2025-01-02T10:00:00')
  },
  {
    tipo: 'Saque',
    valor: -200,
    clienteOrigem: '09506382000',
    clienteDestino: null,
    descricao: 'Saque realizado',
    data: new Date('2025-01-10T10:00:00')
  },
  {
    tipo: 'Depósito',
    valor: 7000,
    clienteOrigem: '09506382000',
    clienteDestino: null,
    descricao: 'Depósito em conta',
    data: new Date('2025-02-05T10:00:00')
  },

  {
    tipo: 'Transferência',
    valor: 1700,
    clienteOrigem: '12912861012',
    clienteDestino: '09506382000',
    descricao: 'De Catharyna (1291)',
    data: new Date('2020-01-20T12:00:00')
  },

  {
    tipo: 'Depósito',
    valor: 1000,
    clienteOrigem: '85733854057',
    clienteDestino: null,
    descricao: 'Depósito em conta',
    data: new Date('2025-05-05')
  },
  {
    tipo: 'Saque',
    valor: -2000,
    clienteOrigem: '85733854057',
    clienteDestino: null,
    descricao: 'Saque realizado',
    data: new Date('2025-05-06')
  },

  {
    tipo: 'Depósito',
    valor: 150000,
    clienteOrigem: '58872160006',
    clienteDestino: null,
    descricao: 'Depósito em conta',
    data: new Date('2025-06-01')
  },

  {
    tipo: 'Depósito',
    valor: 1500,
    clienteOrigem: '76179646090',
    clienteDestino: null,
    descricao: 'Depósito em conta',
    data: new Date('2025-07-01')
  }
];

  const usuarios = [
    {
      cpf: '98574307084',
      nome: 'Geniéve',
      email: 'ger1@bantads.com.br',
      senha: 'tads',
      tipo: 'gerente'
    },
    {
      cpf: '64065268052',
      nome: 'Godophredo',
      email: 'ger2@bantads.com.br',
      senha: 'tads',
      tipo: 'gerente'
    },
    {
      cpf: '23862179060',
      nome: 'Gyândula',
      email: 'ger3@bantads.com.br',
      senha: 'tads',
      tipo: 'gerente'
    },
    {
      cpf: '40501740066',
      nome: 'Adamântio',
      email: 'adm1@bantads.com.br',
      senha: 'tads',
      tipo: 'administrador'
    }
  ];

  localStorage.setItem('clientes', JSON.stringify(clientes));
  localStorage.setItem('contas', JSON.stringify(contas));
  localStorage.setItem('transacoes', JSON.stringify(transacoes));
  localStorage.setItem('usuarios', JSON.stringify(usuarios));
}
}
