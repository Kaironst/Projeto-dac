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

type ModoFormulario = 'novo' | 'editar';

@Component({
  selector: 'app-gerente-form-modal',
  standalone: true,
  imports: [ReactiveFormsModule, MatButtonModule, MatCardModule, MatFormFieldModule, MatInputModule, NgxMaskDirective],
  templateUrl: './gerente-form-modal.html',
  styleUrl: './gerente-form-modal.css',
})
export class GerenteFormModal implements OnChanges {
  @Input() aberto = false;
  @Input() gerente: GerenteCadastro | null = null;
  @Input() modo: ModoFormulario = 'novo';
  @Output() fechar = new EventEmitter<GerenteCadastro | undefined>();
  protected esconderSenha = true;

  protected readonly formGroup = new FormGroup({
    nome: new FormControl('', [Validators.required]),
    cpf: new FormControl('', [Validators.required]),
    email: new FormControl('', [Validators.required, Validators.email]),
    telefone: new FormControl('', [Validators.required]),
    senha: new FormControl('', [Validators.required]),
  });

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['modo']) {
      this.atualizarValidacaoSenha();
      this.atualizarCamposEditaveis();
    }

    if (changes['aberto']?.currentValue || changes['gerente']) {
      this.esconderSenha = true;

      if (this.gerente) {
        this.formGroup.reset({
          nome: this.gerente.nome,
          cpf: this.gerente.cpf,
          email: this.gerente.email,
          telefone: this.gerente.telefone,
          senha: this.modo === 'editar' ? '' : this.gerente.senha,
        });
        return;
      }

      this.formGroup.reset({
        nome: '',
        cpf: '',
        email: '',
        telefone: '',
        senha: '',
      });
    }
  }

  private atualizarValidacaoSenha(): void {
    const senhaControl = this.formGroup.controls.senha;

    if (this.modo === 'editar') {
      senhaControl.clearValidators();
    } else {
      senhaControl.setValidators([Validators.required]);
    }

    senhaControl.updateValueAndValidity({ emitEvent: false });
  }

  private atualizarCamposEditaveis(): void {
    const cpfControl = this.formGroup.controls.cpf;
    const telefoneControl = this.formGroup.controls.telefone;

    if (this.modo === 'editar') {
      cpfControl.disable({ emitEvent: false });
      telefoneControl.disable({ emitEvent: false });
    } else {
      cpfControl.enable({ emitEvent: false });
      telefoneControl.enable({ emitEvent: false });
    }
  }

  protected get titulo(): string {
    return this.modo === 'editar' ? 'Editar Gerente' : 'Adicionar Gerente';
  }

  protected get textoSalvar(): string {
    return this.modo === 'editar' ? 'Salvar alterações' : 'Salvar gerente';
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

  protected alternarVisibilidadeSenha(): void {
    this.esconderSenha = !this.esconderSenha;
  }
}