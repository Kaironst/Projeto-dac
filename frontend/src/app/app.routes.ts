import { Routes } from '@angular/router';

import { TelaPrincipal } from './tela_principal/tela_principal';
import { Autocadastro } from './autocadastro/autocadastro';
import { Login } from './login/login';
import { CrudGerente } from './crud-gerente/crud-gerente';
import { ClienteTela } from './cliente_tela/cliente_tela';
import { ConsultarCliente } from './consultar-cliente/consultar-cliente';
import { AdminTela } from './admin_tela/admin_tela';
import { GerenteTela } from './gerente_tela/gerente_tela';
import { ClientePerfil } from './cliente_perfil/cliente_perfil';

export const routes: Routes = [
    { path:'tela-principal', component:TelaPrincipal},
    { path: '', redirectTo:'tela-principal', pathMatch:'full'},
    { path: 'autocadastro', component: Autocadastro },
    { path: 'login', component: Login },
    { path: 'gerentes', component: CrudGerente },
    { path: 'cliente-tela', component: ClienteTela },
    { path: 'cliente-perfil', component: ClientePerfil },
    { path: 'consultar-cliente', component: ConsultarCliente },
    {path:'admin-tela', component: AdminTela},
    {path:'gerente-tela', component: GerenteTela}
];
