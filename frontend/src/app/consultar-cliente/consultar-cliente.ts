import { ChangeDetectionStrategy, ChangeDetectorRef, Component, inject } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { FormControl, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { NgxMaskDirective, NgxMaskPipe } from 'ngx-mask';
import { firstValueFrom } from 'rxjs';
import { ClienteConsulta, ConsultaClienteUtil } from '../services/DBUtil/consulta-cliente-util';

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
  private consultaClienteUtil = inject(ConsultaClienteUtil);
  private changeDetectorRef = inject(ChangeDetectorRef);

  public formGroup: FormGroup;
  public clienteConsultado: ClienteConsulta | null = null;
  public pesquisado: boolean = false;
  public funcionalidadeAtiva: FuncionalidadeConsulta = null as any;
  public carregando: boolean = false;
  public mensagemErro: string = '';

  public filtroCpfTodos: string = '';
  public filtroNomeTodos: string = '';
  public clienteDetalhes: ClienteConsulta | null = null;
  public melhoresClientes: ClienteConsulta[] = [];

  private clientes: ClienteConsulta[] = [];

  constructor() {
    this.formGroup = new FormGroup({
      cpf: new FormControl('', [Validators.required])
    });

  }

  async consultar() {
    if (!this.formGroup.valid) {
      return;
    }

    this.pesquisado = true;
    this.clienteConsultado = null;
    this.mensagemErro = '';

    const cpfBuscado = this.normalizarCpf(this.formGroup.get('cpf')?.value ?? '');

    await this.executarComCarregamento(async () => {
      try {
        this.clienteConsultado = await firstValueFrom(this.consultaClienteUtil.getByCpf(cpfBuscado));
      } catch (erro) {
        if (erro instanceof HttpErrorResponse && erro.status === 404) {
          this.clienteConsultado = null;
          return;
        }

        this.mensagemErro = 'Nao foi possivel consultar o cliente no backend.';
      }
    });
  }

  async selecionarFuncionalidade(funcionalidade: FuncionalidadeConsulta) {
    this.funcionalidadeAtiva = funcionalidade;
    this.mensagemErro = '';
    this.limparConsultaCpf();
    this.fecharDetalhesCliente();

    if (funcionalidade === 'top3') {
      await this.carregarMelhoresClientes();
    }

    if (funcionalidade === 'todos') {
      await this.carregarClientes();
    }
  }

  private async carregarMelhoresClientes() {
    await this.executarComCarregamento(async () => {
      try {
        this.melhoresClientes = await firstValueFrom(this.consultaClienteUtil.getTopSaldo(3));
      } catch {
        this.melhoresClientes = [];
        this.mensagemErro = 'Nao foi possivel carregar os melhores clientes do backend.';
      }
    });
  }

  private async carregarClientes() {
    await this.executarComCarregamento(async () => {
      try {
        this.clientes = await firstValueFrom(this.consultaClienteUtil.getAll());
      } catch {
        this.clientes = [];
        this.mensagemErro = 'Nao foi possivel carregar os clientes do backend.';
      }
    });
  }

  private async executarComCarregamento(callback: () => Promise<void>) {
    this.carregando = true;
    this.changeDetectorRef.markForCheck();

    try {
      await callback();
    } finally {
      this.carregando = false;
      this.changeDetectorRef.markForCheck();
    }
  }

  get clientesOrdenadosFiltrados(): ClienteConsulta[] {

    const cpfFiltro = this.normalizarCpf(this.filtroCpfTodos);
    const nomeFiltro = this.filtroNomeTodos.trim().toLowerCase();

    return [...this.clientes]
      .sort((a, b) => a.nome.localeCompare(b.nome, 'pt-BR'))
      .filter(cliente => {

        const cpfOk = !cpfFiltro || cliente.cpf.includes(cpfFiltro);
        const nomeOk = !nomeFiltro || cliente.nome.toLowerCase().includes(nomeFiltro);

        return cpfOk && nomeOk;
      });
  }

  abrirDetalhesCliente(cliente: ClienteConsulta) {
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
