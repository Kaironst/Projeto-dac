export interface AuthLoginRequest {
  email: null | string;
  senha: null | string;
}

export interface AuthLoginResponse {
  token: null | string;
  tokenType: null | string;
  tipo: null | string;
  email: null | string;
  clienteId: null | number;
}
