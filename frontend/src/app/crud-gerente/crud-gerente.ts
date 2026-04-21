import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';

import { GerenteFormModal } from './gerente-form-modal/gerente-form-modal';
import { GerenteRemoverModal } from './gerente-remover-modal/gerente-remover-modal';

interface GerenteCadastro {
  id?: number;
  nome: string;
  cpf: string;
  email: string;
  telefone: string;
  senha: string;
}

interface GerenteApiPayload {
  nome: string;
  cpf: string;
  email: string;
  telefone: string;
  senha?: string;
  tipo?: string;
}

@Component({
  selector: 'app-crud-gerente',
  standalone: true,
  imports: [CommonModule, MatButtonModule, MatCardModule, GerenteFormModal, GerenteRemoverModal],
  templateUrl: './crud-gerente.html',
  styleUrl: './crud-gerente.css',
})
export class CrudGerente implements OnInit {
  private readonly gerentesApiUrl = 'http://localhost:8080/gerentes';
  private readonly changeDetectorRef = inject(ChangeDetectorRef);

  protected gerentes: GerenteCadastro[] = [];
  protected modalAberto = false;
  protected modalModo: 'novo' | 'editar' = 'novo';
  protected gerenteSelecionadoParaEdicao: GerenteCadastro | null = null;
  protected cpfOriginalParaEdicao: string | null = null;
  protected confirmacaoRemocaoAberta = false;
  protected gerenteSelecionadoParaRemocao: GerenteCadastro | null = null;
  protected mensagemStatus = '';

  private normalizarCpf(cpf: string): string {
    return cpf.replace(/\D/g, '');
  }

  private atualizarMensagemStatus(mensagem: string): void {
    this.mensagemStatus = mensagem;
    this.changeDetectorRef.detectChanges();
  }

  ngOnInit(): void {
    void this.carregarGerentes();
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

  protected async removerGerenteSelecionado(): Promise<void> {
    if (!this.gerenteSelecionadoParaRemocao) return;

    const gerenteRemovido = this.gerenteSelecionadoParaRemocao;
    this.cancelarRemocao();

    const remocaoConcluida = await this.removerGerenteNaApi(gerenteRemovido);

    if (!remocaoConcluida) {
      this.atualizarMensagemStatus('Nao foi possivel remover gerente no backend.');
      return;
    }

    const listagemAtualizada = await this.carregarGerentes();
    this.atualizarMensagemStatus(listagemAtualizada
      ? 'Gerente excluido com sucesso.'
      : 'Gerente removido, mas nao foi possivel atualizar a listagem.');

    this.cancelarRemocao();
  }

  protected async fecharCadastro(gerente?: GerenteCadastro): Promise<void> {
    this.modalAberto = false;

    if (!gerente) return;

    const cpfNovo = this.normalizarCpf(gerente.cpf);
    const cpfOriginalEdicao = this.cpfOriginalParaEdicao ? this.normalizarCpf(this.cpfOriginalParaEdicao) : null;

    const cpfJaExiste = this.gerentes.some(g =>
      this.normalizarCpf(g.cpf) === cpfNovo && this.normalizarCpf(g.cpf) !== cpfOriginalEdicao
    );

    if (cpfJaExiste) {
      alert('CPF já cadastrado!');
      return;
    }

    if (this.modalModo === 'editar' && this.cpfOriginalParaEdicao) {
      const gerenteOriginal = this.gerentes.find(g => this.normalizarCpf(g.cpf) === cpfOriginalEdicao);

      if (!gerenteOriginal) {
        this.atualizarMensagemStatus('Gerente selecionado para edicao nao foi encontrado na listagem.');
      } else {
        const atualizacaoConcluida = await this.atualizarGerenteNaApi(gerenteOriginal, gerente);

        if (!atualizacaoConcluida) {
          this.atualizarMensagemStatus('Nao foi possivel atualizar gerente no backend.');
          return;
        }

        const listagemAtualizada = await this.carregarGerentes();
        this.atualizarMensagemStatus(listagemAtualizada
          ? 'Gerente alterado com sucesso.'
          : 'Gerente atualizado, mas nao foi possivel atualizar a listagem.');
      }

    } else {
      const cadastroCriado = await this.inserirGerenteNaApi(gerente);

      if (!cadastroCriado) {
        this.atualizarMensagemStatus('Nao foi possivel inserir gerente no banco (RF17).');
        return;
      }

      const listagemAtualizada = await this.carregarGerentes();
      this.atualizarMensagemStatus(listagemAtualizada
        ? 'Gerente criado com sucesso.'
        : 'Gerente inserido, mas nao foi possivel atualizar a listagem.');
    }

    this.modalModo = 'novo';
    this.gerenteSelecionadoParaEdicao = null;
    this.cpfOriginalParaEdicao = null;
  }

  private async carregarGerentes(): Promise<boolean> {
    const gerentesDaApi = await this.buscarGerentesNaApi();

    if (gerentesDaApi) {
      this.gerentes = gerentesDaApi;
      this.changeDetectorRef.detectChanges();
      return true;
    }

    this.gerentes = [];
    this.atualizarMensagemStatus('Nao foi possivel carregar gerentes no backend.');
    return false;
  }

  private async inserirGerenteNaApi(gerente: GerenteCadastro): Promise<boolean> {
    const payload: GerenteApiPayload = {
      nome: gerente.nome,
      cpf: this.normalizarCpf(gerente.cpf),
      email: gerente.email,
      telefone: gerente.telefone,
      senha: gerente.senha,
      tipo: 'GERENTE'
    };

    try {
      const response = await fetch(this.gerentesApiUrl, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(payload)
      });

      return response.ok;
    } catch {
      return false;
    }
  }

  private async atualizarGerenteNaApi(gerenteOriginal: GerenteCadastro, gerenteAtualizado: GerenteCadastro): Promise<boolean> {
    if (gerenteOriginal.id === undefined || gerenteOriginal.id === null) {
      return false;
    }

    const payload: GerenteApiPayload = {
      nome: gerenteAtualizado.nome,
      cpf: this.normalizarCpf(gerenteOriginal.cpf),
      email: gerenteAtualizado.email,
      telefone: gerenteOriginal.telefone
    };

    if (gerenteAtualizado.senha?.trim()) {
      payload.senha = gerenteAtualizado.senha;
    }

    try {
      const response = await fetch(`${this.gerentesApiUrl}/${gerenteOriginal.id}`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(payload)
      });

      return response.ok;
    } catch {
      return false;
    }
  }

  private async removerGerenteNaApi(gerente: GerenteCadastro): Promise<boolean> {
    if (gerente.id === undefined || gerente.id === null) {
      return false;
    }

    try {
      const response = await fetch(`${this.gerentesApiUrl}/${gerente.id}`, {
        method: 'DELETE'
      });

      return response.ok;
    } catch {
      return false;
    }
  }

  private async buscarGerentesNaApi(): Promise<GerenteCadastro[] | null> {
    try {
      const response = await fetch(this.gerentesApiUrl);

      if (!response.ok) {
        return null;
      }

      const payload = await response.json();
      if (!Array.isArray(payload)) {
        return null;
      }

      return payload.map((gerente: Partial<GerenteCadastro>) => ({
        id: gerente.id,
        nome: gerente.nome ?? '',
        cpf: gerente.cpf ?? '',
        email: gerente.email ?? '',
        telefone: gerente.telefone ?? '',
        senha: gerente.senha ?? ''
      }));
    } catch {
      return null;
    }
  }
}