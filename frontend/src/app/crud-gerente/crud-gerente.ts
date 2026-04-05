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
  protected confirmacaoRemocaoAberta = false;
  protected gerenteSelecionadoParaRemocao: GerenteCadastro | null = null;
  protected mensagemStatus = '';

  ngOnInit(): void {
    this.carregarGerentes();
  }

  protected get gerentesOrdenados(): GerenteCadastro[] {
    return [...this.gerentes].sort((primeiro, segundo) =>
      primeiro.nome.localeCompare(segundo.nome, 'pt-BR', { sensitivity: 'base' })
    );
  }

  protected abrirCadastro(): void {
    this.mensagemStatus = '';
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
    if (!this.gerenteSelecionadoParaRemocao) {
      return;
    }

    const nomeRemovido = this.gerenteSelecionadoParaRemocao.nome;
    const cpfRemovido = this.gerenteSelecionadoParaRemocao.cpf;

    this.gerentes = this.gerentes.filter((gerente) => gerente.cpf !== cpfRemovido);
    this.salvarGerentes();
    this.carregarGerentes();
    this.mensagemStatus = `Gerente ${nomeRemovido} removido com sucesso.`;
    this.cancelarRemocao();
  }

  protected fecharCadastro(gerente?: GerenteCadastro): void {
    this.modalAberto = false;

    if (!gerente) {
      return;
    }

    this.gerentes = [...this.gerentes, gerente];
    this.salvarGerentes();
    this.carregarGerentes();
    this.mensagemStatus = `Gerente ${gerente.nome} cadastrado com sucesso.`;
  }

  private carregarGerentes(): void {
    const gerentesSalvos = localStorage.getItem(CHAVE_GERENTES);

    if (!gerentesSalvos) {
      this.gerentes = [];
      return;
    }

    try {
      const gerentes = JSON.parse(gerentesSalvos) as GerenteCadastro[];
      this.gerentes = Array.isArray(gerentes) ? gerentes : [];
    } catch {
      this.gerentes = [];
    }
  }

  private salvarGerentes(): void {
    localStorage.setItem(CHAVE_GERENTES, JSON.stringify(this.gerentes));
  }
}
