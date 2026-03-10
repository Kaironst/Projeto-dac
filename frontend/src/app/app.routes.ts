import { Routes } from '@angular/router';
import { TelaPrincipal } from './tela_principal/tela_principal';

export const routes: Routes = [
    { path:'tela-principal', component:TelaPrincipal},
    { path: '', redirectTo:'tela-principal', pathMatch:'full'},
];
