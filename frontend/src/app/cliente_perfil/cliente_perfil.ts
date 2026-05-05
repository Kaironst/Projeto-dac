import { Component, ChangeDetectorRef, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup } from '@angular/forms';
import { NgxMaskDirective } from 'ngx-mask';

interface Cliente {
  id?: number;
  cpf: string;
  nome: string;
  email: string;
  telefone: string;
  salario: number;
}

@Component({
  selector: 'app-cliente-perfil',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule, NgxMaskDirective],
  templateUrl: './cliente_perfil.html',
  styleUrls: ['./cliente_perfil.css']
})
export class ClientePerfil {

  private readonly apiUrl = 'http://localhost:8080/clientes';
  private readonly changeDetectorRef = inject(ChangeDetectorRef);

  perfilForm: FormGroup;

  cpf: string = '12912861012';
  clienteAtual: Cliente | null = null;

  numConta: string = '';
  gerente: string = '';

  saldo: number = 0;
  limite: number = 0;

  constructor(private fb: FormBuilder) {
    this.perfilForm = this.fb.group({
      nome: [''],
      email: [''],
      telefone: [''],
      salario: [0],

      cep: [''],
      logradouro: [''],
      numero: [''],
      complemento: [''],
      cidade: [''],
      estado: ['']
    });

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

    this.perfilForm.patchValue({
      nome: cliente.nome,
      email: cliente.email,
      telefone: cliente.telefone,
      salario: cliente.salario
    });

    this.limite = this.calcularLimite(cliente.salario);

    // Mock enquanto não integra contas
    this.numConta = '0001';
    this.gerente = 'Gerente Padrão';
    this.saldo = 0;

    this.changeDetectorRef.detectChanges();
  }

  async atualizarPerfil() {
    if (!this.clienteAtual) return;

    const { nome, email, telefone, salario } = this.perfilForm.value;

    if (salario <= 0) {
      alert('Salário inválido!');
      return;
    }

    const atualizado: Cliente = {
      ...this.clienteAtual,
      nome,
      email,
      telefone,
      salario
    };

    const sucesso = await this.atualizarClienteNaApi(atualizado);

    if (!sucesso) {
      alert('Erro ao atualizar!');
      return;
    }

    this.clienteAtual = atualizado;
    this.limite = this.calcularLimite(salario);

    alert('Perfil atualizado!');
  }

  calcularLimite(salario: number): number {
    return salario * 0.5;
  }
}