import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { Router } from '@angular/router';
import { FormControl, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { NgxMaskDirective } from 'ngx-mask';

interface GerenteCadastro {
  nome: string;
  cpf: string;
  email: string;
  telefone: string;
  senha: string;
}

const CHAVE_GERENTES = 'gerentes';

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
    if (this.formGroup.invalid) {
      this.formGroup.markAllAsTouched();
      return;
    }

    const gerentes = this.obterGerentes();
    const novoGerente: GerenteCadastro = {
      nome: this.formGroup.value.nome,
      cpf: this.formGroup.value.cpf,
      email: this.formGroup.value.email,
      telefone: this.formGroup.value.telefone,
      senha: this.formGroup.value.senha,
    };

    gerentes.push(novoGerente);
    localStorage.setItem(CHAVE_GERENTES, JSON.stringify(gerentes));
    this.formGroup.reset();
    this.router.navigate(['/tela-principal']);
    }

  voltar() {
    this.router.navigate(['/tela-principal']);
  }

  private obterGerentes(): GerenteCadastro[] {
    const gerentesSalvos = localStorage.getItem(CHAVE_GERENTES);

    if (!gerentesSalvos) {
      return [];
    }

    try {
      return JSON.parse(gerentesSalvos) as GerenteCadastro[];
    } catch {
      return [];
    }
  }
}