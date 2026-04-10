
export interface UsersDtoMessage {
  operation: null | string;
  data: null | UsersDtoCliente;
}

export interface UsersDtoCliente {
  id: null | number;

  nome: null | string;
  email: null | string;
  cpf: null | string;
  estado: null | number
  telefone: null | string;
  salario: null | number
  enderecos: null | UsersDtoEndereco[];
}

export interface UsersDtoEndereco {
  id: null | number;
  logradouro: null | string;
  numero: null | number;
  complemento: null | string;
  cep: null | string;
  cidade: null | string;
  estado: null | string;

}
