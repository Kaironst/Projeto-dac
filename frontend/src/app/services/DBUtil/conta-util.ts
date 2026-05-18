import { inject, Injectable } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import { Cliente } from "./cliente-util";
import { Gerente } from "./gerente-util";
import { ContatoComBanco } from "./contato-com-banco";

export interface Conta {
  id?: number | null;
  numero?: string | null;
  cliente?: Cliente | null;
  gerente?: Gerente | null;
  saldo?: number | null;
  limite?: number | null;
  dataCriacao?: string | null;
}

@Injectable({
  providedIn: "root",
})
export class ContaUtil extends ContatoComBanco {

  protected requestUrl = "/contas";
  protected http = inject(HttpClient);

  override getAll() {
    return this.http.get<Conta[]>(this.requestUrl);
  }

  override get(id: number) {
    return this.http.get<Conta>(`${this.requestUrl}/${id}`);
  }

  override create(conta: Conta) {
    return this.http.post<unknown>(this.requestUrl, conta);
  }

  override update(id: number, conta: Conta) {
    return this.http.put<Conta>(`${this.requestUrl}/${id}`, conta);
  }

  override delete(id: number) {
    return this.http.delete<Conta>(`${this.requestUrl}/${id}`);
  }

}
