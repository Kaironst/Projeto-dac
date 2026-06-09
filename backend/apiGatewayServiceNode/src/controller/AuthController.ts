import { Router, Request, Response, response } from "express";
import { LoginRequest } from "../dto/LoginRequest";
import { authProducer, gerentesProducerCqrs, usersProducerCqrs } from "../messaging/GenericProducerRPC";
import { TokenDto } from "../dto/TokenDto";
import { validateJwt } from "../auth/JwtUtils";
import { validateHeaderValue } from "http";
import { jwtDecode } from "jwt-decode";
import { UsersDtoCliente } from "../dto/UsersDto";
import { GerentesDtoGerente } from "../dto/GerentesDto";

const router = Router();

//POST /
router.post("/login", async (req: Request, res: Response) => {
  try {
    const loginRequest = req.body as LoginRequest;
    const responseToken = await authProducer.requestService({ operation: "LOGIN", data: [loginRequest], dataType: "login" });
    console.log(`recebido: data = ${responseToken.data} operation =${responseToken.operation}`)
    if (responseToken.operation === "ERROR_NO_LOGIN") {
      res.status(401).json({
        message: "Usuário/Senha incorretos"
      });
      return;
    }

    const decoded = jwtDecode(responseToken.data?.at(0)?.token!);
    const username = decoded.sub!;

    const roles: string[] = (decoded as any).roles || [];
    if (roles.length === 0)
      throw new Error("token inválido (sem roles)")

    let user: UsersDtoCliente | GerentesDtoGerente | null = null;
    let tipo: string | null = null;

    if (roles.includes("ROLE_CLIENTE")) {
      user = (await usersProducerCqrs.requestService({ operation: "READ_BY_EMAIL", data: [{ email: username } as UsersDtoCliente], dataType: "cliente" })).data![0];
      tipo = "CLIENTE"
    }
    else if (roles.includes("ROLE_GERENTE") || roles.includes("ROLE_ADMINISTRADOR")) {
      user = (await gerentesProducerCqrs.requestService({ operation: "READ_BY_EMAIL", data: [{ email: username } as GerentesDtoGerente], dataType: "gerente" })).data![0];
      tipo = roles.includes("ROLE_ADMINISTRADOR") ? "ADMINISTRADOR" : "GERENTE"
    }

    const loginResponse = {
      access_token: responseToken.data?.at(0)?.token,
      token_type: "bearer",

      tipo: tipo,

      usuario: user

    };

    res.status(200).json(loginResponse);
  } catch (error) {
    res.sendStatus(500);
  }
});

router.post("/validate", async (req: Request, res: Response) => {
  try {

    const body = req.body as { token: string };
    const validated = validateJwt(body.token);

    validated ? res.status(200).json(true) :
      res.status(401).json(false);

  }
  catch (error) {
    res.sendStatus(500);
  }
})

export default router;
