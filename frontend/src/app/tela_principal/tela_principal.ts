import {Component} from '@angular/core';
import { RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-tela_principal',
  templateUrl: './tela_principal.html',
  styleUrl: './tela_principal.css',
  standalone: true,
  imports: [
    RouterOutlet
  ]
})

export class TelaPrincipal {
}
