import { Component, ChangeDetectorRef, DestroyRef, inject } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup } from '@angular/forms';
import { NgxMaskDirective } from 'ngx-mask';
import { catchError, debounceTime, distinctUntilChanged, EMPTY, filter, map, switchMap } from 'rxjs';
import { CepService } from '../services/cep.service';
import { UFS } from '../shared/ufs';
import { Auth } from '../services/auth/auth';
import { HttpClient } from '@angular/common/http';

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

  private readonly consultasUrl ='http://localhost:8080/consultas/clientes/cpf';
  private readonly clientesUrl = 'http://localhost:8080/clientes';
  private readonly changeDetectorRef = inject(ChangeDetectorRef);
  private readonly cepService = inject(CepService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly auth = inject(Auth);
  private readonly http = inject(HttpClient);

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



  carregarDadosCliente() {
  this.http.get<any>(
    `${this.consultasUrl}/${this.cpf}`
  ).subscribe({
    next: (consulta) => {

      this.clienteAtual = {
        id: consulta.id,
        cpf: consulta.cpf,
        nome: consulta.nome,
        email: consulta.email,
        telefone: consulta.telefone,
        salario: consulta.salario
      };

      const endereco = consulta.endereco ?? {};

      this.perfilForm.patchValue({
        nome: consulta.nome,
        email: consulta.email,
        telefone: consulta.telefone,
        salario: consulta.salario,

        cep: endereco.cep,
        logradouro: endereco.logradouro,
        numero: endereco.numero,
        complemento: endereco.complemento,
        cidade: endereco.cidade,
        estado: endereco.estado
      });

      if (consulta.conta) {
        this.numConta = consulta.conta.id;

        this.saldo = consulta.conta.saldo;

        this.limite = consulta.conta.limite;

        this.gerente = consulta.conta.gerenteId
          ? `ID: ${consulta.conta.gerenteId}`
          : 'Não atribuído';
      }

      this.changeDetectorRef.detectChanges();
    },

    error: (erro) => {
      console.error(
        'Erro ao carregar perfil:',
        erro
      );
    }
  });
}

  atualizarPerfil() {

  if (!this.clienteAtual) {
    return;
  }

  const {
  nome,
  email,
  telefone,
  salario,
  cep,
  logradouro,
  numero,
  complemento,
  cidade,
  estado
} = this.perfilForm.value;

  if (salario <= 0) {
    alert('Salário inválido!');
    return;
  }

  const atualizado = {
  ...this.clienteAtual,

  nome,
  email,
  telefone,
  salario,

  enderecos: [
    {
      logradouro,
      numero,
      complemento,
      cep,
      cidade,
      estado
    }
  ]
};

  this.http.put(
    `${this.clientesUrl}/${this.clienteAtual.id}`,
    atualizado
  ).subscribe({

    next: () => {

      this.clienteAtual = atualizado;

      this.limite =
        this.calcularLimite(salario);

      alert('Perfil atualizado!');
      this.carregarDadosCliente();
    },

    error: (erro) => {
      console.error(
        'Erro ao atualizar perfil:',
        erro
      );

      alert('Erro ao atualizar!');
    }
  });

  window.location.reload();
}

  calcularLimite(salario: number): number {
    return salario * 0.5;
  }

  private normalizarCep(cep: string): string {
    return cep.replace(/\D/g, '');
  }
}
