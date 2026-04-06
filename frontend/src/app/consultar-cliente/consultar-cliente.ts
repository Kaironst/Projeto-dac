import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { FormControl, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { NgxMaskDirective, NgxMaskPipe } from 'ngx-mask';

interface ClienteMock {
  nome: string;
  email: string;
  cpf: string;
  telefone: string;
  salario: number;
  endereco: {
    cep: string;
    logradouro: string;
    numero: string;
    complemento: string;
    cidade: string;
    estado: string;
  };
  conta: {
    saldo: number;
    limite: number;
  };
}

type FuncionalidadeConsulta = 'cpf' | 'top3' | 'todos';

@Component({
  selector: 'app-consultar-cliente',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatFormFieldModule,
    MatInputModule,
    ReactiveFormsModule,
    MatButtonModule,
    MatCardModule,
    NgxMaskDirective,
    NgxMaskPipe
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './consultar-cliente.html',
  styleUrl: './consultar-cliente.css'
})
export class ConsultarCliente {

  private router = inject(Router);

  public formGroup: FormGroup;
  public clienteConsultado: ClienteMock | null = null;
  public pesquisado: boolean = false;
  public funcionalidadeAtiva: FuncionalidadeConsulta = null as any;

  public filtroCpfTodos: string = '';
  public filtroNomeTodos: string = '';
  public clienteDetalhes: ClienteMock | null = null;

  private clientes: any[] = [];
  private contas: any[] = [];

  constructor() {
    this.formGroup = new FormGroup({
      cpf: new FormControl('', [Validators.required])
    });

    this.carregarDados();
  }

  carregarDados() {
    this.clientes = JSON.parse(localStorage.getItem('clientes') || '[]');
    this.contas = JSON.parse(localStorage.getItem('contas') || '[]');
  }

  montarCliente(cliente: any): ClienteMock {

    const conta = this.contas.find(c => c.clienteCpf === cliente.cpf);

    return {
      nome: cliente.nome,
      email: cliente.email,
      cpf: cliente.cpf,
      telefone: cliente.telefone || '',
      salario: cliente.salario,
      endereco: cliente.endereco || {
        cep: '',
        logradouro: '',
        numero: '',
        complemento: '',
        cidade: '',
        estado: ''
      },
      conta: {
        saldo: conta?.saldo ?? 0,
        limite: conta?.limite ?? 0
      }
    };
  }

  consultar() {
    if (this.formGroup.valid) {
      this.pesquisado = true;

      const cpfBuscado = this.normalizarCpf(this.formGroup.get('cpf')?.value ?? '');

      const cliente = this.clientes.find(c => c.cpf === cpfBuscado);

      this.clienteConsultado = cliente ? this.montarCliente(cliente) : null;
    }
  }

  selecionarFuncionalidade(funcionalidade: FuncionalidadeConsulta) {
    this.funcionalidadeAtiva = funcionalidade;
    this.limparConsultaCpf();
    this.fecharDetalhesCliente();
  }

  get melhoresClientes(): ClienteMock[] {
    return this.clientes
      .map(c => this.montarCliente(c))
      .sort((a, b) => b.conta.saldo - a.conta.saldo)
      .slice(0, 3);
  }

  get clientesOrdenadosFiltrados(): ClienteMock[] {

    const cpfFiltro = this.normalizarCpf(this.filtroCpfTodos);
    const nomeFiltro = this.filtroNomeTodos.trim().toLowerCase();

    return this.clientes
      .map(c => this.montarCliente(c))
      .sort((a, b) => a.nome.localeCompare(b.nome, 'pt-BR'))
      .filter(cliente => {

        const cpfOk = !cpfFiltro || cliente.cpf.includes(cpfFiltro);
        const nomeOk = !nomeFiltro || cliente.nome.toLowerCase().includes(nomeFiltro);

        return cpfOk && nomeOk;
      });
  }

  abrirDetalhesCliente(cliente: ClienteMock) {
    this.clienteDetalhes = cliente;
  }

  fecharDetalhesCliente() {
    this.clienteDetalhes = null;
  }

  private limparConsultaCpf() {
    this.pesquisado = false;
    this.clienteConsultado = null;
    this.formGroup.reset();
  }

  private normalizarCpf(cpf: string): string {
    return cpf.replace(/\D/g, '');
  }

  voltar() {
    this.router.navigate(['/tela-principal']);
  }
}