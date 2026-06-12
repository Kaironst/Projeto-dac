import { Router, Request, Response } from "express";
import { contasProducer } from "../messaging/GenericProducerRPC";
import { ContasDtoConta } from "../dto/ContasDto";

const router = Router();

router.post("/depositar", async (req: Request, res: Response) => {
  try {
    const { numero, valor } = req.body;
    if (!numero || !valor || valor <= 0) {
      return res.status(400).json({ message: "Número da conta e valor positivo são obrigatórios." });
    }

    const contaDto: ContasDtoConta = { id: 0, numero: numero, saldo: valor } as ContasDtoConta;

    const response = await contasProducer.requestService({ operation: "DEPOSITO", data: [contaDto], dataType: "conta" });

    if (response.operation === "RESULT") {
      res.status(200).json(response.data?.at(0));
    } else {
      res.status(500).json({ message: "Erro ao depositar." });
    }
  } catch (error) {
    console.error("Depositar Error:", error);
    res.sendStatus(500);
  }
});

router.post("/sacar", async (req: Request, res: Response) => {
  try {
    const { numero, valor } = req.body;
    if (!numero || !valor || valor <= 0) {
      return res.status(400).json({ message: "Número da conta e valor positivo são obrigatórios." });
    }

    const contaDto: ContasDtoConta = { id: 0, numero: numero, saldo: valor } as ContasDtoConta;

    const response = await contasProducer.requestService({ operation: "SAQUE", data: [contaDto], dataType: "conta" });

    if (response.operation === "RESULT") {
      res.status(200).json(response.data?.at(0));
    } else {
      res.status(400).json({ message: "Erro ao sacar. Verifique seu saldo." });
    }
  } catch (error) {
    console.error("Sacar Error:", error);
    res.sendStatus(500);
  }
});

router.post("/transferir", async (req: Request, res: Response) => {
  try {
    const { numeroOrigem, numeroDestino, valor } = req.body;
    if (!numeroOrigem || !numeroDestino || !valor || valor <= 0) {
      return res.status(400).json({ message: "Contas e valor positivo são obrigatórios." });
    }

    const contaOrigem: ContasDtoConta = { id: 0, numero: numeroOrigem, saldo: valor } as ContasDtoConta;
    const contaDestino: ContasDtoConta = { id: 0, numero: numeroDestino, saldo: valor } as ContasDtoConta;

    const response = await contasProducer.requestService({ operation: "TRANSFERENCIA", data: [contaOrigem, contaDestino], dataType: "conta" });

    if (response.operation === "RESULT") {
      res.status(200).json(response.data?.at(0));
    } else {
      res.status(400).json({ message: "Erro ao transferir. Verifique seu saldo e conta de destino." });
    }
  } catch (error) {
    console.error("Transferir Error:", error);
    res.sendStatus(500);
  }
});

export default router;
