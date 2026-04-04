import { HttpClient } from "@angular/common/http";
import { catchError, firstValueFrom, of, timeout } from "rxjs";

export abstract class ContatoComBanco {

  protected abstract requestUrl: string;
  protected abstract http: HttpClient;

  //trocar o any pelo tipo da interface exportada em cada uma das subclasses

  //retorna todos os membros do banco de dados como observable<tipo[]>
  abstract getAll(): any;

  //retorna um objeto do banco de dados como observable<tipo>
  abstract get(id: number): any;

  //retorna o objeto inserido no banco como observable<tipo>
  abstract create(obj: object): any;

  //retorna o objeto atualizado como observable<tipo>
  abstract update(id: number, obj: object): any;

  //retorna o objeto deletado como observable<tipo>
  abstract delete(id: number): any

  //retorna estado do banco de dados como boolean (online ou não)
  async ping(): Promise<boolean> {

    try {
      await firstValueFrom(
        this.http.get(this.requestUrl).pipe(
          timeout(2000),
          catchError(() => of(null))
        )
      );
      return true;
    }
    catch {
      return false;
    }

  }

}
