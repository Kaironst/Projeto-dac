import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';

import { GerenteFormModal } from './gerente-form-modal/gerente-form-modal';
import { GerenteRemoverModal } from './gerente-remover-modal/gerente-remover-modal';

interface GerenteCadastro {
  nome: string;
  cpf: string;
  email: string;
  telefone: string;
  senha: string;
}

const CHAVE_GERENTES = 'gerentes';
const CHAVE_USUARIOS = 'usuarios';

@Component({
  selector: 'app-crud-gerente',
  standalone: true,
  imports: [CommonModule, MatButtonModule, MatCardModule, GerenteFormModal, GerenteRemoverModal],
  templateUrl: './crud-gerente.html',
  styleUrl: './crud-gerente.css',
})
export class CrudGerente implements OnInit {
  protected gerentes: GerenteCadastro[] = [];
  protected modalAberto = false;
  protected modalModo: 'novo' | 'editar' = 'novo';
  protected gerenteSelecionadoParaEdicao: GerenteCadastro | null = null;
  protected cpfOriginalParaEdicao: string | null = null;
  protected confirmacaoRemocaoAberta = false;
  protected gerenteSelecionadoParaRemocao: GerenteCadastro | null = null;
  protected mensagemStatus = '';

  ngOnInit(): void {
    this.carregarGerentes();
  }

  protected get gerentesOrdenados(): GerenteCadastro[] {
    return [...this.gerentes].sort((a, b) =>
      a.nome.localeCompare(b.nome, 'pt-BR', { sensitivity: 'base' })
    );
  }

  protected abrirCadastro(): void {
    this.mensagemStatus = '';
    this.modalModo = 'novo';
    this.gerenteSelecionadoParaEdicao = null;
    this.cpfOriginalParaEdicao = null;
    this.modalAberto = true;
  }

  protected abrirEdicao(gerente: GerenteCadastro): void {
    this.mensagemStatus = '';
    this.modalModo = 'editar';
    this.gerenteSelecionadoParaEdicao = { ...gerente };
    this.cpfOriginalParaEdicao = gerente.cpf;
    this.modalAberto = true;
  }

  protected abrirConfirmacaoRemocao(gerente: GerenteCadastro): void {
    this.mensagemStatus = '';
    this.gerenteSelecionadoParaRemocao = gerente;
    this.confirmacaoRemocaoAberta = true;
  }

  protected cancelarRemocao(): void {
    this.confirmacaoRemocaoAberta = false;
    this.gerenteSelecionadoParaRemocao = null;
  }

  protected removerGerenteSelecionado(): void {
    if (!this.gerenteSelecionadoParaRemocao) return;

    const cpf = this.gerenteSelecionadoParaRemocao.cpf;

    this.gerentes = this.gerentes.filter(g => g.cpf !== cpf);

    const usuarios = JSON.parse(localStorage.getItem(CHAVE_USUARIOS) || '[]');
    const novosUsuarios = usuarios.filter((u: any) => u.cpf !== cpf);

    localStorage.setItem(CHAVE_USUARIOS, JSON.stringify(novosUsuarios));

    this.salvarGerentes();

    this.mensagemStatus = `Gerente ${this.gerenteSelecionadoParaRemocao.nome} removido com sucesso.`;
    this.cancelarRemocao();
  }

  protected fecharCadastro(gerente?: GerenteCadastro): void {
    this.modalAberto = false;

    if (!gerente) return;

    const usuarios = JSON.parse(localStorage.getItem(CHAVE_USUARIOS) || '[]');

    const cpfJaExiste = this.gerentes.some(g =>
      g.cpf === gerente.cpf && g.cpf !== this.cpfOriginalParaEdicao
    );

    if (cpfJaExiste) {
      alert('CPF já cadastrado!');
      return;
    }

    if (this.modalModo === 'editar' && this.cpfOriginalParaEdicao) {

      this.gerentes = this.gerentes.map(g =>
        g.cpf === this.cpfOriginalParaEdicao ? gerente : g
      );

      const novosUsuarios = usuarios.map((u: any) =>
        u.cpf === this.cpfOriginalParaEdicao
          ? { ...u, nome: gerente.nome, email: gerente.email, senha: gerente.senha }
          : u
      );

      localStorage.setItem(CHAVE_USUARIOS, JSON.stringify(novosUsuarios));

      this.mensagemStatus = `Gerente ${gerente.nome} atualizado com sucesso.`;

    } else {

      this.gerentes.push(gerente);

      usuarios.push({
        cpf: gerente.cpf,
        nome: gerente.nome,
        email: gerente.email,
        senha: gerente.senha,
        tipo: 'gerente'
      });

      localStorage.setItem(CHAVE_USUARIOS, JSON.stringify(usuarios));

      this.mensagemStatus = `Gerente ${gerente.nome} cadastrado com sucesso.`;
    }

    this.salvarGerentes();

    this.modalModo = 'novo';
    this.gerenteSelecionadoParaEdicao = null;
    this.cpfOriginalParaEdicao = null;
  }

  private carregarGerentes(): void {
    const gerentesSalvos = localStorage.getItem(CHAVE_GERENTES);

    if (!gerentesSalvos) {

      this.gerentes = [
        {
          cpf: '98574307084',
          nome: 'Geniéve',
          email: 'ger1@bantads.com.br',
          telefone: '',
          senha: 'tads'
        },
        {
          cpf: '64065268052',
          nome: 'Godophredo',
          email: 'ger2@bantads.com.br',
          telefone: '',
          senha: 'tads'
        },
        {
          cpf: '23862179060',
          nome: 'Gyândula',
          email: 'ger3@bantads.com.br',
          telefone: '',
          senha: 'tads'
        }
      ];

      this.salvarGerentes();
      this.sincronizarUsuarios();
      return;
    }

    try {
      const gerentes = JSON.parse(gerentesSalvos);
      this.gerentes = Array.isArray(gerentes) ? gerentes : [];
    } catch {
      this.gerentes = [];
    }
  }

  private salvarGerentes(): void {
    localStorage.setItem(CHAVE_GERENTES, JSON.stringify(this.gerentes));
  }

  private sincronizarUsuarios() {
    const usuarios = JSON.parse(localStorage.getItem(CHAVE_USUARIOS) || '[]');

    this.gerentes.forEach(g => {
      const existe = usuarios.some((u: any) => u.cpf === g.cpf);

      if (!existe) {
        usuarios.push({
          cpf: g.cpf,
          nome: g.nome,
          email: g.email,
          senha: g.senha,
          tipo: 'gerente'
        });
      }
    });

    localStorage.setItem(CHAVE_USUARIOS, JSON.stringify(usuarios));
  }
}