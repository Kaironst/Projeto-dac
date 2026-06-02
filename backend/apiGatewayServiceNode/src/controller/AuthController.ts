import { Router, Request, Response } from "express";
import { LoginRequest } from "../dto/LoginRequest";
import { authProducer } from "../messaging/GenericProducerRPC";

const router = Router();

//POST /
router.post("/", async (req: Request, res: Response) => {
  try {
    const loginRequest = req.body as LoginRequest;
    const responseToken = await authProducer.requestService({ operation: "LOGIN", data: [loginRequest] });
    console.log(`recebido: data = ${responseToken.data} operation =${responseToken.operation}`)
    if (responseToken.operation === "ERROR_NO_LOGIN") {
      res.status(401).json({
        message: "Usuário/Senha incorretos"
      });
      return;
    }

    const loginResponse = {
      access_token: responseToken.data?.at(0)?.token,
      token_type: "bearer",

      tipo: "CLIENTE",

      usuario: {
        nome: "Catharyna",
        cpf: "12912861012",
        email: "cli1@bantads.com.br"
      }
    };

    res.status(200).json(loginResponse);
  } catch (error) {
    res.sendStatus(500);
  }
});

export default router;
