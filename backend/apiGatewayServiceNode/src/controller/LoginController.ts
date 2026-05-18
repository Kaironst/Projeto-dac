import { Router, Request, Response } from "express";
import { AuthLoginRequest } from "../dto/AuthDto";
import { authProducer } from "../messaging/GenericProducerRPC";

const router = Router();

router.post("/", async (req: Request, res: Response) => {
  try {
    const loginRequest: AuthLoginRequest = {
      email: typeof req.body.email === "string" ? req.body.email : null,
      senha: typeof req.body.senha === "string" ? req.body.senha : null
    };

    if (!loginRequest.email || !loginRequest.senha) {
      res.status(400).json({ message: "Email e senha sao obrigatorios." });
      return;
    }

    const authMessage = await authProducer.requestService({
      operation: "LOGIN",
      data: [loginRequest]
    });

    if (authMessage.operation !== "RESULT" || !authMessage.data?.[0]) {
      res.status(401).json({ message: "Credenciais invalidas." });
      return;
    }

    res.status(200).json(authMessage.data[0]);
  } catch (error) {
    res.sendStatus(500);
  }
});

export default router;
