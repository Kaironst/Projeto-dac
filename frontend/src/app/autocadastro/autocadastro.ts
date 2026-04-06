import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { Router } from '@angular/router';
import { FormControl, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { NgxMaskDirective } from 'ngx-mask';

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

    const pedidos = JSON.parse(localStorage.getItem('pedidosCadastro') || '[]');

    const cpfExiste = pedidos.some((p: any) => p.cpf === this.normalizarCpf(form.cpf));

    if (cpfExiste) {
      alert('Já existe um pedido com esse CPF!');
      return;
    }

    const novoPedido = {
      nome: form.nome,
      email: form.email,
      cpf: this.normalizarCpf(form.cpf),
      telefone: form.telefone,
      salario: Number(form.salario),
      endereco: {
        cep: form.cep,
        logradouro: form.logradouro,
        numero: form.numero,
        complemento: form.complemento,
        cidade: form.cidade,
        estado: form.estado
      },
      dataSolicitacao: new Date(),
      status: 'pendente'
    };

    pedidos.push(novoPedido);

    localStorage.setItem('pedidosCadastro', JSON.stringify(pedidos));

    this.mostrarMensagemSucesso = true;
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
}