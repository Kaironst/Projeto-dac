import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { Router } from '@angular/router';
import { FormControl, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { NgxMaskDirective } from 'ngx-mask';
import { HttpErrorResponse } from '@angular/common/http';
import { ClienteUtil } from '../services/DBUtil/cliente-util';

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
    NgxMaskDirective
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './autocadastro.html',
  styleUrl: './autocadastro.css'
})
export class Autocadastro {
  private router = inject(Router);
  private clienteUtil = inject(ClienteUtil);
  public formGroup: FormGroup;
  public mostrarMensagemSucesso = false;

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
  }

  cadastrar() {
    if (this.formGroup.invalid) {
      this.formGroup.markAllAsTouched();
      return;
    }

    const form = this.formGroup.value;

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
      },
      error: (erro: HttpErrorResponse) => {
        console.error('Erro detalhado do backend:', erro);

        if (erro.status === 0) {
          alert('Nao foi possivel conectar com o backend. Verifique se a API gateway esta rodando na porta 8080.');
          return;
        }

        alert('Erro ao realizar autocadastro. Verifique os logs do backend para mais detalhes.');
      }
    });
  }

  fecharMensagemSucesso() {
    this.mostrarMensagemSucesso = false;
    this.router.navigate(['/tela-principal']);
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
}