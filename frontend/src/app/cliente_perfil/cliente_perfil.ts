import { Component, ChangeDetectorRef, DestroyRef, inject } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup } from '@angular/forms';
import { NgxMaskDirective } from 'ngx-mask';
import { catchError, debounceTime, distinctUntilChanged, EMPTY, filter, map, switchMap } from 'rxjs';
import { CepService } from '../services/cep.service';
import { UFS } from '../shared/ufs';
import { Auth } from '../services/auth/auth';

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
  private readonly cepService = inject(CepService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly auth = inject(Auth);

  perfilForm: FormGroup;

  cpf: string = '';
  clienteAtual: Cliente | null = null;

  numConta: string = '';
  gerente: string = '';

  saldo: number = 0;
  limite: number = 0;
  readonly ufs = UFS;

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

    this.configurarAutocompleteCep();
    this.auth.getClienteAtual().subscribe(cliente => {
      if (cliente && cliente.cpf) {
        this.cpf = cliente.cpf;
        this.carregarDadosCliente();
      }
    });
  }

  private configurarAutocompleteCep(): void {
    const cepControl = this.perfilForm.get('cep');

    cepControl?.valueChanges.pipe(
      debounceTime(300),
      map((cep: string | null) => this.normalizarCep(cep ?? '')),
      distinctUntilChanged(),
      filter((cep) => cep.length === 8),
      switchMap((cep) => this.cepService.fetchAddressByCep(cep).pipe(
        catchError(() => {
          cepControl.setErrors({ cepNaoEncontrado: true });
          this.perfilForm.patchValue({
            logradouro: '',
            cidade: '',
            estado: ''
          }, { emitEvent: false });
          this.changeDetectorRef.detectChanges();
          return EMPTY;
        })
      )),
      takeUntilDestroyed(this.destroyRef)
    ).subscribe((address) => {
      this.perfilForm.patchValue({
        cep: address.cep,
        logradouro: address.logradouro,
        cidade: address.cidade,
        estado: address.estado
      }, { emitEvent: false });

      cepControl.setErrors(null);
      this.changeDetectorRef.detectChanges();
    });
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

  private normalizarCep(cep: string): string {
    return cep.replace(/\D/g, '');
  }
}
