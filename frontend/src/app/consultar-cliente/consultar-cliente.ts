import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { FormControl, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { NgxMaskDirective, NgxMaskPipe } from 'ngx-mask';

interface ClienteMock {
  nome: string;
  email: string;
  cpf: string;
  telefone: string;
  salario: number;
  endereco: {
    cep: string;
    logradouro: string;
    numero: string;
    complemento: string;
    cidade: string;
    estado: string;
  };
  conta: {
    saldo: number;
    limite: number;
  };
}

@Component({
  selector: 'app-consultar-cliente',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatFormFieldModule,
    MatInputModule,
    ReactiveFormsModule,
    MatButtonModule,
    MatCardModule,
    NgxMaskDirective,
    NgxMaskPipe
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './consultar-cliente.html',
  styleUrl: './consultar-cliente.css'
})

export class ConsultarCliente {
  private router = inject(Router);
  public formGroup: FormGroup;
  public clienteConsultado: ClienteMock | null = null;
  public pesquisado: boolean = false;

  // dados mockados para teste
  private mockDatabase: ClienteMock[] = [
    {
      nome: 'João da Silva',
      email: 'joao@email.com',
      cpf: '11111111111',
      telefone: '41999999999',
      salario: 5000,
      endereco: {
        cep: '80000000',
        logradouro: 'Rua das Flores',
        numero: '123',
        complemento: 'Apto 4',
        cidade: 'Curitiba',
        estado: 'PR'
      },
      conta: {
        saldo: 1500.50,
        limite: 1000.00
      }
    }
  ];

  constructor() {
    this.formGroup = new FormGroup({
      cpf: new FormControl('', [Validators.required])
    });
  }

  consultar() {
    if (this.formGroup.valid) {
      this.pesquisado = true;
      const cpfBuscado = this.formGroup.get('cpf')?.value;
      this.clienteConsultado = this.mockDatabase.find(c => c.cpf === cpfBuscado) || null;
    }
  }

  voltar() {
    this.router.navigate(['/tela-principal']);
  }
}
