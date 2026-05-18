import { ChangeDetectionStrategy, ChangeDetectorRef, Component, inject } from '@angular/core';
import { Router } from '@angular/router';
import { FormControl, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';

interface LoginResponse {
  token: string;
  tokenType: string;
  tipo: string;
  email: string;
  clienteId: number;
}

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    FormsModule,
    MatFormFieldModule,
    MatInputModule,
    ReactiveFormsModule,
    MatButtonModule,
    MatCardModule
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './login.html',
  styleUrl: './login.css'
})

export class Login {
  private router = inject(Router);
  private http = inject(HttpClient);
  private changeDetectorRef = inject(ChangeDetectorRef);
  public formGroup: FormGroup;
  public mostrarMensagemSucesso = false;
  public mostrarMensagemErro = false;
  public mensagemErro = '';
  public loginResponse: LoginResponse | null = null;

  constructor() {
    this.formGroup = new FormGroup({
      email: new FormControl('', [Validators.required, Validators.email]),
      senha: new FormControl('', [Validators.required])
    });
  }

  login() {
    if (this.formGroup.invalid) {
      this.formGroup.markAllAsTouched();
      return;
    }

    this.mostrarMensagemSucesso = false;
    this.mostrarMensagemErro = false;
    this.mensagemErro = '';

    this.http.post<LoginResponse>('/login', this.formGroup.value).subscribe({
      next: (response) => {
        this.loginResponse = response;
        this.mostrarMensagemSucesso = true;
        this.changeDetectorRef.markForCheck();
      },
      error: (erro: HttpErrorResponse) => {
        this.loginResponse = null;
        this.mensagemErro = erro.status === 401
          ? 'Email ou senha invalidos.'
          : 'Nao foi possivel realizar login no backend.';
        this.mostrarMensagemErro = true;
        this.changeDetectorRef.markForCheck();
      }
    });
  }

  fecharMensagemSucesso() {
    this.mostrarMensagemSucesso = false;
    const tipo = this.loginResponse?.tipo?.toUpperCase();
    if (tipo === 'GERENTE') {
      this.router.navigate(['/gerente-tela']);
      return;
    }

    if (tipo === 'ADMIN' || tipo === 'ADMINISTRADOR') {
      this.router.navigate(['/admin-tela']);
      return;
    }

    this.router.navigate(['/cliente-tela']);
  }

  fecharMensagemErro() {
    this.mostrarMensagemErro = false;
  }

  voltar() {
    this.router.navigate(['/tela-principal']);
  }
}
