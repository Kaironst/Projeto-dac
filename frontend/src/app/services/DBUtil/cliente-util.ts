import { inject, Injectable } from "@angular/core";
import { ContatoComBanco } from "./contato-com-banco";
import { HttpClient } from "@angular/common/http";

export interface Cliente {
  id?: number | null;
  nome?: string | null;
  email?: string | null;
  cpf?: string | null;
  telefone?: string | null;
  estado?: number | null;
  salario?: number | null;

  // a ser implementado
  //endereco?: Endereco|null;

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
