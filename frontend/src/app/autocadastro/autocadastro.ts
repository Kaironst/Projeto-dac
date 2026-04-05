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

    this.mostrarMensagemSucesso = true;
  }

  fecharMensagemSucesso() {
    this.mostrarMensagemSucesso = false;
    this.router.navigate(['/tela-principal']);
  }

  voltar() {
    this.router.navigate(['/tela-principal']);
  }
}