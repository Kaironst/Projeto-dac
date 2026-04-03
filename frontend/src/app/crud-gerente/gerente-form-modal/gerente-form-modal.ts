import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { NgxMaskDirective } from 'ngx-mask';

interface GerenteCadastro {
  nome: string;
  cpf: string;
  email: string;
  telefone: string;
  senha: string;
}

@Component({
  selector: 'app-gerente-form-modal',
  standalone: true,
  imports: [ReactiveFormsModule, MatButtonModule, MatCardModule, MatFormFieldModule, MatInputModule, NgxMaskDirective],
  templateUrl: './gerente-form-modal.html',
  styleUrl: './gerente-form-modal.css',
})
export class GerenteFormModal implements OnChanges {
  @Input() aberto = false;
  @Output() fechar = new EventEmitter<GerenteCadastro | undefined>();

  protected readonly formGroup = new FormGroup({
    nome: new FormControl('', [Validators.required]),
    cpf: new FormControl('', [Validators.required]),
    email: new FormControl('', [Validators.required, Validators.email]),
    telefone: new FormControl('', [Validators.required]),
    senha: new FormControl('', [Validators.required]),
  });

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['aberto']?.currentValue) {
      this.formGroup.reset({
        nome: '',
        cpf: '',
        email: '',
        telefone: '',
        senha: '',
      });
    }
  }

  protected salvar(): void {
    if (this.formGroup.invalid) {
      this.formGroup.markAllAsTouched();
      return;
    }

    this.fechar.emit(this.formGroup.getRawValue() as GerenteCadastro);
  }

  protected cancelar(): void {
    this.fechar.emit();
  }

  protected impedirFechamento(evento: MouseEvent): void {
    evento.stopPropagation();
  }
}