import { GerentesDtoGerente } from "./GerentesDto";
import { UsersDtoCliente } from "./UsersDto";

export interface ContasDtoConta {
  id: null | number;
  numero: null | string;
  cliente: null | UsersDtoCliente;
  gerente: null | GerentesDtoGerente;
  saldo: null | number;
  limite: null | number;
  dataCriacao: null | string;
}
