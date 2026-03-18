import { Routes } from '@angular/router';

import { TelaPrincipal } from './tela_principal/tela_principal';
import { Autocadastro } from './autocadastro/autocadastro';
import { Login } from './login/login';
import { InserirGerente } from './crud_gerente/inserir-gerente/inserir-gerente';

export const routes: Routes = [
    { path:'tela-principal', component:TelaPrincipal},
    { path: '', redirectTo:'tela-principal', pathMatch:'full'},
    { path: 'autocadastro', component: Autocadastro },
    { path: 'login', component: Login },
    { path: 'inserir-gerente', component: InserirGerente }
];
