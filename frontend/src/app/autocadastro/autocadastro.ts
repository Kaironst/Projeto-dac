import { ChangeDetectionStrategy, ChangeDetectorRef, Component, DestroyRef, inject } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Router } from '@angular/router';
import { FormControl, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatSelectModule } from '@angular/material/select';
import { NgxMaskDirective } from 'ngx-mask';
import { HttpErrorResponse } from '@angular/common/http';
import { catchError, debounceTime, distinctUntilChanged, EMPTY, filter, map, switchMap } from 'rxjs';
import { ClienteUtil } from '../services/DBUtil/cliente-util';
import { CepService } from '../services/cep.service';
import { UFS } from '../shared/ufs';

@Component({
  selector: 'app-autocadastro',
  standalone: true,
  imports: [
    FormsModule,
    MatFormFieldModule,
    MatInputModule,
    ReactiveFormsModule,
    MatButtonModule,
    MatCardModule,
    MatSelectModule,
    NgxMaskDirective
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './autocadastro.html',
  styleUrl: './autocadastro.css'
})
export class Autocadastro {
  private router = inject(Router);
  private clienteUtil = inject(ClienteUtil);
  private cepService = inject(CepService);
  private destroyRef = inject(DestroyRef);
  private changeDetectorRef = inject(ChangeDetectorRef);
  public formGroup: FormGroup;
  public mostrarMensagemSucesso = false;
  public mostrarMensagemErro = false;
  public mensagemErro = '';
  public readonly ufs = UFS;
  
  constructor() {
    this.formGroup = new FormGroup({
      nome: new FormControl('', [Validators.required]),
      email: new FormControl('', [Validators.required, Validators.email]),
      cpf: new FormControl('', [Validators.required]),
      telefone: new FormControl('', [Validators.required]),
      salario: new FormControl('', [Validators.required]),
      cep: new FormControl('', [Validators.required]),
      logradouro: new FormControl('', [Validators.required]),
      numero: new FormControl('', [Validators.required]),
      complemento: new FormControl(''),
      cidade: new FormControl('', [Validators.required]),
      estado: new FormControl('', [Validators.required])
      
    });

    this.configurarAutocompleteCep();
  }

  private configurarAutocompleteCep(): void {
    const cepControl = this.formGroup.get('cep');

    cepControl?.valueChanges.pipe(
      debounceTime(300),
      map((cep: string | null) => this.normalizarCep(cep ?? '')),
      distinctUntilChanged(),
      filter((cep) => cep.length === 8),
      switchMap((cep) => this.cepService.fetchAddressByCep(cep).pipe(
        catchError(() => {
          cepControl.setErrors({ cepNaoEncontrado: true });
          this.formGroup.patchValue({
            logradouro: '',
            cidade: '',
            estado: ''
          }, { emitEvent: false });
          this.changeDetectorRef.markForCheck();
          return EMPTY;
        })
      )),
      takeUntilDestroyed(this.destroyRef)
    ).subscribe((address) => {
      this.formGroup.patchValue({
        cep: address.cep,
        logradouro: address.logradouro,
        cidade: address.cidade,
        estado: address.estado
      }, { emitEvent: false });

      cepControl.setErrors(null);
      this.changeDetectorRef.markForCheck();
    });
  }

  async cadastrar() {
    if (this.formGroup.invalid) {
      this.formGroup.markAllAsTouched();
      return;
    }
    this.mostrarMensagemSucesso = false;
    

    this.mostrarMensagemErro = false;
    this.mensagemErro = '';

    const form = this.formGroup.value;
    const temporizador = new Temporizador();

    const novoCliente = {
      nome: form.nome,
      email: form.email,
      cpf: this.normalizarCpf(form.cpf),
      telefone: this.normalizarTelefone(form.telefone),
      salario: Number(form.salario),
      enderecos: [
        {
          cep: form.cep,
          logradouro: form.logradouro,
          numero: String(form.numero),
          complemento: form.complemento || null,
          cidade: form.cidade,
          estado: form.estado
        }
      ]
    };

    this.clienteUtil.create(novoCliente).subscribe({
      next: () => {
        this.mostrarMensagemSucesso = true;
        this.changeDetectorRef.markForCheck();
      },
      error: (erro: HttpErrorResponse) => {
        console.error('Erro detalhado do backend:', erro);
        temporizador.cancelar();

        if (erro.status === 0) {
          this.mensagemErro = 'Nao foi possivel conectar com o backend. Verifique se a API gateway esta rodando na porta 8080.';
          this.mostrarMensagemErro = true;
          this.changeDetectorRef.markForCheck();
          return;
        }

        if (erro.status === 409) {
          this.mensagemErro = erro.error?.message ?? 'CPF já cadastrado ou aguardando aprovação.';
          this.mostrarMensagemErro = true;
          this.changeDetectorRef.markForCheck();
          return;
        }

        this.mensagemErro = erro.error?.message ?? 'Erro ao realizar autocadastro. Verifique os logs do backend para mais detalhes.';
        this.mostrarMensagemErro = true;
        this.changeDetectorRef.markForCheck();
      }

    });

  temporizador.iniciar(3000, () => {
    this.mostrarMensagemSucesso = true;
  });


  }

  fecharMensagemSucesso() {
    this.mostrarMensagemSucesso = false;
    this.router.navigate(['/tela-principal']);
  }

  fecharMensagemErro() {
    this.mostrarMensagemErro = false;
  }

  voltar() {
    this.router.navigate(['/tela-principal']);
  }

  private normalizarCpf(cpf: string): string {
    return cpf.replace(/\D/g, '');
  }

  private normalizarTelefone(telefone: string): string {
    return telefone.replace(/\D/g, '');
  }

  private normalizarCep(cep: string): string {
    return cep.replace(/\D/g, '');
  }
}

class Temporizador {

  private timeoutId: ReturnType<typeof setTimeout> | null = null;

  iniciar(delayMs: number, callback: () => void): void {
    this.cancelar();

    this.timeoutId = setTimeout(() => {
      this.timeoutId = null;
      callback();
    }, delayMs);
  }

  cancelar(): void {
    if (this.timeoutId) {
      clearTimeout(this.timeoutId);
      this.timeoutId = null;
    }
  }

}