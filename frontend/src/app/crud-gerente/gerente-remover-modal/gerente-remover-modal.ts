import { Component, EventEmitter, Input, Output } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';

@Component({
  selector: 'app-gerente-remover-modal',
  standalone: true,
  imports: [MatButtonModule, MatCardModule],
  templateUrl: './gerente-remover-modal.html',
  styleUrl: './gerente-remover-modal.css',
})
export class GerenteRemoverModal {
  @Input() aberto = false;
  @Input() gerenteNome = '';

  @Output() confirmar = new EventEmitter<void>();
  @Output() cancelar = new EventEmitter<void>();

  protected onCancelar(): void {
    this.cancelar.emit();
  }

  protected onConfirmar(): void {
    this.confirmar.emit();
  }

  protected impedirFechamento(evento: MouseEvent): void {
    evento.stopPropagation();
  }
}