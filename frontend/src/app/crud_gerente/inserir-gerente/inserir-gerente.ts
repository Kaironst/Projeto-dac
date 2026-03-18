import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { Router } from '@angular/router';
import { FormControl, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { NgxMaskDirective } from 'ngx-mask';

@Component({
  selector: 'app-login',
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
  templateUrl: './inserir-gerente.html',
  styleUrl: './inserir-gerente.css'
})

export class InserirGerente {
  private router = inject(Router);
  public formGroup: FormGroup;

  constructor() {
    this.formGroup = new FormGroup({
      nome: new FormControl('', [Validators.required]),
      cpf: new FormControl('', [Validators.required]),
      email: new FormControl('', [Validators.required, Validators.email]),
      telefone: new FormControl('', [Validators.required]),
      senha: new FormControl('', [Validators.required])
    });
  }

  inserir() {
    // a ser implementado
    }

  voltar() {
    this.router.navigate(['/tela-principal']);
  }
}