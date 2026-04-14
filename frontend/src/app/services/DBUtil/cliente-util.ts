import { inject, Injectable } from "@angular/core";
import { ContatoComBanco } from "./contato-com-banco";
import { HttpClient } from "@angular/common/http";

export interface Cliente {
  id?: number;
  nome?: string;
  email?: string;
  cpf?: string;
  telefone?: string;
  estado?: number;
  salario?: number;
  enderecos?: Endereco[];
}

export interface Endereco {
  id?: number;
  rua?: string;
  numero?: string;
  bairro?: string;
  cidade?: string;
  estado?: string;
}

@Injectable({
  providedIn: "root",
})
export class ClienteUtil extends ContatoComBanco {

  protected requestUrl = "http://localhost8081/api/clientes";

  protected http = inject(HttpClient);

  override getAll() {
    return this.http.get<Cliente[]>(this.requestUrl);
  }

  override get(id: number) {
    return this.http.get<Cliente>(`${this.requestUrl}/${id}`);
  }

  override create(cliente: Cliente) {
    return this.http.post<Cliente>(this.requestUrl, cliente);
  }

  override update(id: number, cliente: Cliente) {
    return this.http.put<Cliente>(`${this.requestUrl}/${id}`, cliente);
  }

  override delete(id: number) {
    return this.http.delete<Cliente>(`${this.requestUrl}/${id}`);
  }

}
