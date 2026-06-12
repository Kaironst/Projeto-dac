import { jwtDecode } from "jwt-decode"
import { HttpClient, HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { catchError, first, map, Observable, of, tap, throwError } from 'rxjs';
import { ActivatedRouteSnapshot, CanActivateFn } from "@angular/router";
import { Cliente, ClienteUtil } from "../DBUtil/cliente-util";
import { Gerente, GerenteUtil } from "../DBUtil/gerente-util";

export interface LoginRequest {
  username: string;
  password: string;
}

interface LoginResponse {
  access_token: string,
  token_type: string,
  tipo: string,
  usuario: Gerente | Cliente | null
};

//Função Guarda
export const authGuard: CanActivateFn = (route: ActivatedRouteSnapshot) => {
  const auth = inject(Auth);
  const cargosDeAcesso = route.data['roles'] as string[];

  if (!auth.isLoggedIn()) {
    console.warn("não há login ativo");
    alert("Por favor, realize login");
    return of(false);
  }

  return auth.validateToken().pipe(
    map((tokenValido) => {

      if (!tokenValido) {
        console.warn("token inválido");
        alert("token inválido");
        auth.logout();
        return false;
      }

      const cargos = auth.getRoles();

      let acesso: boolean = (cargosDeAcesso === null || cargosDeAcesso.length === 0 || cargosDeAcesso.some(cargo => cargos.includes(cargo)));

      if (!acesso) {
        console.warn("acesso negado");
        alert("ação não autorizada");
        return false;
      }

      return true;

    })
  );

}


//interceptor de requisições http que insere e gerencia o token por requisição
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  console.log("interceptor Called");
  const auth = inject(Auth);
  let newReq = req;
  //clona o token e continua os filtros com o novo token
  if (auth.isLoggedIn()) {
    newReq = newReq.clone({
      setHeaders: {
        Authorization: `Bearer ${auth.getToken()}`,
      },
    });
  }
  //continua os interceptors já que não havia um usuário
  return next(newReq)
    //checa se o token se tornou inválido
    .pipe(catchError((err: HttpErrorResponse) => {
      if (err.status === 401) {
        console.warn("token inválido ou vencido. Deslogando");
        auth.logout();
      }
      if (err.status === 403) {
        alert("ação não autorizada");
      }
      return throwError(() => err);
    }));
}

// serviço simples que essencialmente coloca, remove e acessa o token jwt do login dentro do sistema
@Injectable({
  providedIn: 'root'
})
export class Auth {

  private loginApiUrl = "http://localhost:8080/login";
  private validateApiUrl = "http://localhost:8080/validate"

  clienteUtil = inject(ClienteUtil);
  gerenteUtil = inject(GerenteUtil);
  http = inject(HttpClient);

  login(request: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(this.loginApiUrl, request).pipe(
      tap((response) => {
        localStorage.setItem("token", response.access_token);
      })
    );
  }

  validateToken(): Observable<boolean> {
    if (!this.isLoggedIn())
      return of(false).pipe(first());
    let token: string = localStorage.getItem("token")!;
    return this.http.post<boolean>(this.validateApiUrl, token).pipe(first());
  }

  getRoles(): string[] {
    const token = this.getToken();
    if (!this.isLoggedIn())
      return [];
    try {
      let payload: any = jwtDecode(token!);
      return payload.roles || [];
    }
    catch (e) {
      console.warn("token inválido", e);
      return [];
    }
  }

  getEmail(): string {
    const token = this.getToken();
    if (!this.isLoggedIn())
      return "";
    try {
      let payload = jwtDecode(token!);
      return payload.sub!;
    }
    catch (e) {
      console.error("falha ao obter email");
      return "";
    }
  }

  getGerenteAtual(): Observable<Gerente | null> {
    if (!this.getRoles().includes("ROLE_GERENTE"))
      return of(null);
    return this.gerenteUtil.getByEmail(this.getEmail()).pipe(map((res: any) => Array.isArray(res) ? res[0] : res));
  }

  getClienteAtual(): Observable<Cliente | null> {
    if (!this.getRoles().includes("ROLE_CLIENTE")) {
      return of(null);
    }
    return this.clienteUtil.getByEmail(this.getEmail()).pipe(map((res: any) => Array.isArray(res) ? res[0] : res));
  }

  logout() {
    localStorage.removeItem("token");
  }

  getToken(): string | null {
    return localStorage.getItem("token");
  }

  isLoggedIn(): boolean {
    return this.getToken() != null;
  }

}
