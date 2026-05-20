import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';

export interface AddressInfo {
  cep: string;
  logradouro: string;
  bairro: string;
  cidade: string;
  estado: string;
}

type ViaCepResponse = {
  cep: string;
  logradouro: string;
  bairro: string;
  localidade: string;
  uf: string;
  erro?: boolean;
};

function normalizeCep(cep: string): string {
  return cep.replace(/\D/g, '');
}

@Injectable({
  providedIn: 'root'
})
export class CepService {
  private readonly http = inject(HttpClient);

  fetchAddressByCep(cep: string): Observable<AddressInfo> {
    const normalizedCep = normalizeCep(cep);

    if (normalizedCep.length !== 8) {
      throw new Error('CEP deve conter 8 digitos');
    }

    return this.http.get<ViaCepResponse>(`https://viacep.com.br/ws/${normalizedCep}/json/`).pipe(
      map((data) => {
        if (data.erro) {
          throw new Error('CEP nao encontrado');
        }

        return {
          cep: normalizeCep(data.cep),
          logradouro: data.logradouro,
          bairro: data.bairro,
          cidade: data.localidade,
          estado: data.uf
        };
      })
    );
  }
}
