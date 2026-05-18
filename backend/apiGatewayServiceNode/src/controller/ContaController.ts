import { Router, Request, Response } from "express";
import { ContasDtoConta } from "../dto/ContasDto";
import { contasProducer } from "../messaging/GenericProducerRPC";

const router = Router();

router.get("/:id", async (req: Request, res: Response) => {
  try {
    const targetConta = { id: parseInt(req.params.id) } as ContasDtoConta;
    const contasMessage = await contasProducer.requestService({ operation: "READ", data: [targetConta] });
    res.status(200).json(contasMessage.data);
  } catch (error) {
    res.sendStatus(500);
  }
});

router.get("/", async (req: Request, res: Response) => {
  try {
    const contasMessage = await contasProducer.requestService({ operation: "READ_ALL", data: null });
    res.status(200).json(contasMessage.data);
  } catch (error) {
    res.sendStatus(500);
  }
});

router.post("/", async (req: Request, res: Response) => {
  try {
    const newConta = req.body as ContasDtoConta;
    const contasMessage = await contasProducer.requestService({ operation: "CREATE", data: [newConta] });
    res.status(201).json(contasMessage.data);
  } catch (error) {
    res.sendStatus(500);
  }
});

router.put("/:id", async (req: Request, res: Response) => {
  try {
    const newConta = req.body as ContasDtoConta;
    newConta.id = parseInt(req.params.id);
    const contasMessage = await contasProducer.requestService({ operation: "UPDATE", data: [newConta] });
    res.status(200).json(contasMessage.data);
  } catch (error) {
    res.sendStatus(500);
  }
});

router.delete("/:id", async (req: Request, res: Response) => {
  try {
    const targetConta = { id: parseInt(req.params.id) } as ContasDtoConta;
    await contasProducer.requestService({ operation: "DELETE", data: [targetConta] });
    res.sendStatus(204);
  } catch (error) {
    res.sendStatus(500);
  }
});

export default router;
