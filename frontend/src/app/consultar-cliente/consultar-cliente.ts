import { ChangeDetectionStrategy, ChangeDetectorRef, Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { FormControl, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { NgxMaskDirective, NgxMaskPipe } from 'ngx-mask';
import { forkJoin } from 'rxjs';
import { Cliente, ClienteUtil, Endereco } from '../services/DBUtil/cliente-util';
import { Conta, ContaUtil } from '../services/DBUtil/conta-util';

interface ClienteConsulta {
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
  private changeDetectorRef = inject(ChangeDetectorRef);
  private clienteUtil = inject(ClienteUtil);
  private contaUtil = inject(ContaUtil);

  public formGroup: FormGroup;
  public clienteConsultado: ClienteConsulta | null = null;
  public pesquisado = false;
  public funcionalidadeAtiva: FuncionalidadeConsulta | null = null;

  public filtroCpfTodos = '';
  public filtroNomeTodos = '';
  public clienteDetalhes: ClienteConsulta | null = null;
  public carregando = false;
  public mensagemErro = '';

  private clientes: Cliente[] = [];
  private contas: Conta[] = [];

  constructor() {
    this.formGroup = new FormGroup({
      cpf: new FormControl('', [Validators.required])
    });

    this.carregarDados();
  }

  carregarDados(): void {
    this.carregando = true;
    this.mensagemErro = '';

    forkJoin({
      clientes: this.clienteUtil.getAll(),
      contas: this.contaUtil.getAll()
    }).subscribe({
      next: ({ clientes, contas }) => {
        this.clientes = clientes ?? [];
        this.contas = contas ?? [];
        this.carregando = false;
        this.changeDetectorRef.markForCheck();
      },
      error: (erro) => {
        console.error('Erro ao carregar clientes/contas:', erro);
        this.clientes = [];
        this.contas = [];
        this.carregando = false;
        this.mensagemErro = 'Nao foi possivel carregar dados do backend.';
        this.changeDetectorRef.markForCheck();
      }
    });
  }

  montarCliente(cliente: Cliente): ClienteConsulta {
    const conta = this.contas.find(item => item.cliente?.id === cliente.id);
    const endereco = cliente.enderecos?.[0];

    return {
      nome: cliente.nome ?? '',
      email: cliente.email ?? '',
      cpf: cliente.cpf ?? '',
      telefone: cliente.telefone ?? '',
      salario: cliente.salario ?? 0,
      endereco: this.montarEndereco(endereco),
      conta: {
        saldo: conta?.saldo ?? 0,
        limite: conta?.limite ?? 0
      }
    };
  }

  consultar(): void {
    if (this.formGroup.valid) {
      this.pesquisado = true;

      const cpfBuscado = this.normalizarCpf(this.formGroup.get('cpf')?.value ?? '');
      const cliente = this.clientes.find(item => item.cpf === cpfBuscado);

      this.clienteConsultado = cliente ? this.montarCliente(cliente) : null;
    }
  }

  selecionarFuncionalidade(funcionalidade: FuncionalidadeConsulta): void {
    this.funcionalidadeAtiva = funcionalidade;
    this.limparConsultaCpf();
    this.fecharDetalhesCliente();
  }

  get melhoresClientes(): ClienteConsulta[] {
    return this.clientes
      .map(cliente => this.montarCliente(cliente))
      .sort((a, b) => b.conta.saldo - a.conta.saldo)
      .slice(0, 3);
  }

  get clientesOrdenadosFiltrados(): ClienteConsulta[] {
    const cpfFiltro = this.normalizarCpf(this.filtroCpfTodos);
    const nomeFiltro = this.filtroNomeTodos.trim().toLowerCase();

    return this.clientes
      .map(cliente => this.montarCliente(cliente))
      .sort((a, b) => a.nome.localeCompare(b.nome, 'pt-BR'))
      .filter(cliente => {
        const cpfOk = !cpfFiltro || cliente.cpf.includes(cpfFiltro);
        const nomeOk = !nomeFiltro || cliente.nome.toLowerCase().includes(nomeFiltro);

        return cpfOk && nomeOk;
      });
  }

  abrirDetalhesCliente(cliente: ClienteConsulta): void {
    this.clienteDetalhes = cliente;
  }

  fecharDetalhesCliente(): void {
    this.clienteDetalhes = null;
  }

  private montarEndereco(endereco?: Endereco): ClienteConsulta['endereco'] {
    return {
      cep: endereco?.cep ?? '',
      logradouro: endereco?.logradouro ?? '',
      numero: endereco?.numero === null || endereco?.numero === undefined ? '' : String(endereco.numero),
      complemento: endereco?.complemento ?? '',
      cidade: endereco?.cidade ?? '',
      estado: endereco?.estado ?? ''
    };
  }

  private limparConsultaCpf(): void {
    this.pesquisado = false;
    this.clienteConsultado = null;
    this.formGroup.reset();
  }

  private normalizarCpf(cpf: string): string {
    return cpf.replace(/\D/g, '');
  }

  voltar(): void {
    this.router.navigate(['/tela-principal']);
  }
}
