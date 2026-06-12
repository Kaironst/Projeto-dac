import { HttpClient } from "@angular/common/http";
import { inject, Injectable } from "@angular/core";

export interface ClienteConsulta {
  id: number | null;
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
    id: number | null;
    numero: string;
    saldo: number;
    limite: number;
    gerenteId: number | null;
    dataCriacao: string | null;
  };
}

@Injectable({
  providedIn: "root",
})
export class ConsultaClienteUtil {
  private readonly http = inject(HttpClient);
  private readonly requestUrl = "http://localhost:8080/consultas/clientes";

  getAll() {
    return this.http.get<ClienteConsulta[]>(this.requestUrl);
  }

  getByCpf(cpf: string) {
    return this.http.get<ClienteConsulta>(`${this.requestUrl}/cpf/${cpf}`);
  }

  getTopSaldo(limit = 3) {
    return this.http.get<ClienteConsulta[]>(`${this.requestUrl}/top-saldo`, {
      params: {
        limit,
      },
    });
  }
}
