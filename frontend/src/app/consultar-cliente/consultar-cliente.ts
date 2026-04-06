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

type FuncionalidadeConsulta = 'cpf' | 'top3' | 'todos';

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
  public funcionalidadeAtiva: FuncionalidadeConsulta = null as any;

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
    },
    {
      nome: 'Maria Oliveira',
      email: 'maria@email.com',
      cpf: '22222222222',
      telefone: '41988887777',
      salario: 8200,
      endereco: {
        cep: '80220020',
        logradouro: 'Avenida Brasil',
        numero: '452',
        complemento: '',
        cidade: 'Florianópolis',
        estado: 'SC'
      },
      conta: {
        saldo: 9800.75,
        limite: 3000.00
      }
    },
    {
      nome: 'Carlos Almeida',
      email: 'carlos@email.com',
      cpf: '33333333333',
      telefone: '51977776666',
      salario: 6100,
      endereco: {
        cep: '90110090',
        logradouro: 'Rua Padre Chagas',
        numero: '78',
        complemento: 'Sala 12',
        cidade: 'Porto Alegre',
        estado: 'RS'
      },
      conta: {
        saldo: 4200.00,
        limite: 1800.00
      }
    },
    {
      nome: 'Ana Souza',
      email: 'ana@email.com',
      cpf: '44444444444',
      telefone: '11966665555',
      salario: 7300,
      endereco: {
        cep: '01310100',
        logradouro: 'Alameda Santos',
        numero: '1020',
        complemento: 'Apto 81',
        cidade: 'Sao Paulo',
        estado: 'SP'
      },
      conta: {
        saldo: 12350.90,
        limite: 4000.00
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
      const cpfBuscado = this.normalizarCpf(this.formGroup.get('cpf')?.value ?? '');
      this.clienteConsultado = this.mockDatabase.find(c => c.cpf === cpfBuscado) || null;
    }
  }

  selecionarFuncionalidade(funcionalidade: FuncionalidadeConsulta) {
    this.funcionalidadeAtiva = funcionalidade;
    this.limparConsultaCpf();
  }

  get melhoresClientes(): ClienteMock[] {
    return [...this.mockDatabase]
      .sort((a, b) => b.conta.saldo - a.conta.saldo)
      .slice(0, 3);
  }

  private limparConsultaCpf() {
    this.pesquisado = false;
    this.clienteConsultado = null;
    this.formGroup.reset();
  }

  private normalizarCpf(cpf: string): string {
    return cpf.replace(/\D/g, '');
  }

  voltar() {
    this.router.navigate(['/tela-principal']);
  }
}
