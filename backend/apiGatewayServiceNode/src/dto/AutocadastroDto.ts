import { GerentesDtoGerente } from "./GerentesDto";
import { UsersDtoCliente } from "./UsersDto";

export type StatusSolicitacaoAutocadastro =
  | "PENDENTE"
  | "APROVADO"
  | "REJEITADO"
  | "FALHA";

export type TipoNotificacaoAutocadastro =
  | "SOLICITACAO_RECEBIDA"
  | "APROVACAO"
  | "REJEICAO"
  | "FALHA";

export interface AutocadastroSolicitacaoEntrada {
  cliente: UsersDtoCliente;
}

export interface AutocadastroSolicitacao {
  id: null | number;
  cliente: null | UsersDtoCliente;
  gerente: null | GerentesDtoGerente;
  status: null | StatusSolicitacaoAutocadastro;
  motivoRejeicao: null | string;
  dataSolicitacao: null | string;
  dataAnalise: null | string;
}

export interface AutocadastroAprovacao {
  solicitacaoId: null | number;
  cpf: null | string;
  gerenteId: null | number;
}

export interface AutocadastroRejeicao {
  solicitacaoId: number;
  gerenteId: null | number;
  motivo: string;
}

export interface AutocadastroContaCriada {
  solicitacaoId: number;
  conta: unknown;
}

export interface AutocadastroUsuarioAuth {
  solicitacaoId: number;
  clienteId: number;
  email: string;
  senhaTemporaria: string;
  tipo: "CLIENTE";
}

export interface AutocadastroNotificacao {
  solicitacaoId: null | number;
  destinatario: string;
  assunto: string;
  conteudoHtml: string;
  tipo: TipoNotificacaoAutocadastro;
}

export interface AutocadastroFalha {
  solicitacaoId: null | number;
  cpf: null | string;
  email: null | string;
  motivo: string;
  operacaoOrigem: string;
}
