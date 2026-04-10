import { Router, Request, Response } from "express";
import { usersProducerRpc } from "../messaging/UsersProducerRPC"
import { UsersDtoCliente } from "../dto/UsersDto";

const router = Router();

//GET /id
router.get("/:id", async (req: Request, res: Response) => {
  try {
    const targetCliente = { id: parseInt(req.params.id) } as UsersDtoCliente;
    const clientesMessage = await usersProducerRpc.requestOrchestratorService("READ", [targetCliente]);
    res.status(200).json(clientesMessage.data);
  } catch (error) {
    res.sendStatus(500);
  }
});

router.get("/", async (req: Request, res: Response) => {
  try {
    const clientesMessage = await usersProducerRpc.requestOrchestratorService("READ_ALL", null);
    res.status(200).json(clientesMessage.data);
  } catch (error) {
    res.sendStatus(500);
  }
});

router.post("/", async (req: Request, res: Response) => {
  try {
    const newCliente = req.body as UsersDtoCliente;
    console.log("enviando: ", req.body);
    const clientesMessage = await usersProducerRpc.requestOrchestratorService("CREATE", [newCliente]);
    res.sendStatus(201);
  } catch (error) {
    res.sendStatus(500);
  }
});

router.put("/:id", async (req: Request, res: Response) => {
  try {
    const newCliente = req.body as UsersDtoCliente;
    newCliente.id = parseInt(req.params.id);
    console.log("enviando: ", req.body);
    const clientesMessage = await usersProducerRpc.requestOrchestratorService("UPDATE", [newCliente]);
    res.status(200).json(clientesMessage.data);
  } catch (error) {
    res.sendStatus(500);
  }
});

router.delete("/:id", async (req: Request, res: Response) => {
  try {
    const targetCliente = { id: parseInt(req.params.id) } as UsersDtoCliente;
    const clientesMessage = await usersProducerRpc.requestOrchestratorService("DELETE", [targetCliente]);
    res.sendStatus(204);
  } catch (error) {
    res.sendStatus(500);
  }
});

export default router;
