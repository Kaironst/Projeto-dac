import { ChangeDetectionStrategy, ChangeDetectorRef, Component, inject } from '@angular/core';
import { Router } from '@angular/router';
import { FormControl, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { Auth, LoginRequest } from '../services/auth/auth';

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
  private auth = inject(Auth);
  private changeDetectorRef = inject(ChangeDetectorRef);
  public formGroup: FormGroup;
  public mostrarMensagemSucesso = false;

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

    const loginRequest: LoginRequest = {
      username: this.formGroup.value.email,
      password: this.formGroup.value.senha,
    };

    this.auth.login(loginRequest).subscribe({
      next: (res) => {
        console.log("login ok", res)
        this.mostrarMensagemSucesso = true
        this.changeDetectorRef.markForCheck();
      },
      error: (err) => {
        console.error(err);
        alert(err.error?.message)
      }
    })

  }

  fecharMensagemSucesso() {
    this.mostrarMensagemSucesso = false;
    this.router.navigate(['/cliente-tela']);
  }

  voltar() {
    this.router.navigate(['/tela-principal']);
  }
}
