
export interface GerentesDtoMessage {
  operation: null | string;
  data: null | GerentesDtoGerente[];
}

export interface GerentesDtoGerente {
  id: null | number;

  nome: null | string;
  email: null | string;
  cpf: null | string;
  telefone: null | string;
}
