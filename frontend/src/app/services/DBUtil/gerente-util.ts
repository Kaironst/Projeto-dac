import { inject, Injectable } from "@angular/core";
import { ContatoComBanco } from "./contato-com-banco";
import { HttpClient } from "@angular/common/http";

export interface Gerente {
  id?: null | number;

  nome?: null | string;
  email?: null | string;
  cpf?: null | string;
  telefone?: null | string;
  administrador?: null | boolean;
}

@Injectable({
  providedIn: "root",
})
export class GerenteUtil extends ContatoComBanco {

  protected requestUrl = "http://localhost:8080/gerentes";

  protected http = inject(HttpClient);

  override getAll() {
    return this.http.get<Gerente[]>(this.requestUrl);
  }

  override get(id: number) {
    return this.http.get<Gerente>(`${this.requestUrl}/${id}`);
  }

  getByEmail(email: string) {
    return this.http.get<Gerente>(`${this.requestUrl}/email/${email}`);
  }

  getByCpf(cpf: string) {
    return this.http.get<Gerente>(`${this.requestUrl}/cpf/${cpf}`);
  }

  override create(gerente: Gerente) {
    return this.http.post<unknown>(this.requestUrl, gerente);
  }

  override update(id: number, gerente: Gerente) {
    return this.http.put<Gerente>(`${this.requestUrl}/${id}`, gerente);
  }

  override delete(id: number) {
    return this.http.delete<Gerente>(`${this.requestUrl}/${id}`);
  }

}
