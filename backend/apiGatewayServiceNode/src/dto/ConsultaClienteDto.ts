export interface ConsultaClienteDto {
  id: null | number;
  nome: string;
  email: string;
  cpf: string;
  telefone: string;
  salario: number;
  endereco: {
    cep: string;
    logradouro: string;
    numero: string;
    complemento: string;
    cidade: string;
    estado: string;
  };
  conta: {
    id: null | number;
    numero: string;
    saldo: number;
    limite: number;
    gerenteId: null | number;
    dataCriacao: null | string;
  };
}
