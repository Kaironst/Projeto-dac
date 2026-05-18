import { inject, Injectable } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import { ContatoComBanco } from "./contato-com-banco";

export interface Gerente {
  id?: number | null;
  nome?: string | null;
  email?: string | null;
  cpf?: string | null;
  telefone?: string | null;
  administrador?: boolean | null;
}

@Injectable({
  providedIn: "root",
})
export class GerenteUtil extends ContatoComBanco {

  protected requestUrl = "/gerentes";
  protected http = inject(HttpClient);

  override getAll() {
    return this.http.get<Gerente[]>(this.requestUrl);
  }

  override get(id: number) {
    return this.http.get<Gerente>(`${this.requestUrl}/${id}`);
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
