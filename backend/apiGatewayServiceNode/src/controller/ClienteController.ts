import { Router, Request, Response } from "express";
import { UsersDtoCliente } from "../dto/UsersDto";
import { usersProducer } from "../messaging/GenericProducerRPC";

const router = Router();

//GET /id
router.get("/:id", async (req: Request, res: Response) => {
  try {
    const targetCliente = { id: parseInt(req.params.id) } as UsersDtoCliente;
    const clientesMessage = await usersProducer.requestService({ operation: "READ", data: [targetCliente] });
    res.status(200).json(clientesMessage.data);
  } catch (error) {
    res.sendStatus(500);
  }
});

router.get("/", async (req: Request, res: Response) => {
  try {
    const clientesMessage = await usersProducer.requestService({ operation: "READ_ALL", data: null });
    res.status(200).json(clientesMessage.data);
  } catch (error) {
    res.sendStatus(500);
  }
});

router.post("/", async (req: Request, res: Response) => {
  try {
    const newCliente = req.body as UsersDtoCliente;
    console.log("enviando: ", req.body);
    const clientesMessage = await usersProducer.requestService({ operation: "CREATE", data: [newCliente] });
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
    const clientesMessage = await usersProducer.requestService({ operation: "UPDATE", data: [newCliente] });
    res.status(200).json(clientesMessage.data);
  } catch (error) {
    res.sendStatus(500);
  }
});

router.delete("/:id", async (req: Request, res: Response) => {
  try {
    const targetCliente = { id: parseInt(req.params.id) } as UsersDtoCliente;
    const clientesMessage = await usersProducer.requestService({ operation: "DELETE", data: [targetCliente] });
    res.sendStatus(204);
  } catch (error) {
    res.sendStatus(500);
  }
});

export default router;
