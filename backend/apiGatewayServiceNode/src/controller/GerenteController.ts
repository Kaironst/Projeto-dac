import { Router, Request, Response } from "express";
import { GerentesDtoGerente } from "../dto/GerentesDto";
import { GerentesProducer } from "../messaging/GenericProducerRPC";

const router = Router();

//GET /id
router.get("/:id", async (req: Request, res: Response) => {
  try {
    const targetGerente = { id: parseInt(req.params.id) } as GerentesDtoGerente;
    const gerentesMessage = await GerentesProducer.requestService({ operation: "READ", data: [targetGerente] });
    res.status(200).json(gerentesMessage.data);
  } catch (error) {
    res.sendStatus(500);
  }
});

router.get("/", async (req: Request, res: Response) => {
  try {
    const gerentesMessage = await GerentesProducer.requestService({ operation: "READ_ALL", data: null });
    res.status(200).json(gerentesMessage.data);
  } catch (error) {
    res.sendStatus(500);
  }
});

router.post("/", async (req: Request, res: Response) => {
  try {
    const newGerente = req.body as GerentesDtoGerente;
    console.log("enviando: ", req.body);
    const gerentesMessage = await GerentesProducer.requestService({ operation: "CREATE", data: [newGerente] });
    res.sendStatus(201);
  } catch (error) {
    res.sendStatus(500);
  }
});

router.put("/:id", async (req: Request, res: Response) => {
  try {
    const newGerente = req.body as GerentesDtoGerente;
    newGerente.id = parseInt(req.params.id);
    console.log("enviando: ", req.body);
    const gerentesMessage = await GerentesProducer.requestService({ operation: "UPDATE", data: [newGerente] });
    res.status(200).json(gerentesMessage.data);
  } catch (error) {
    res.sendStatus(500);
  }
});

router.delete("/:id", async (req: Request, res: Response) => {
  try {
    const targetGerente = { id: parseInt(req.params.id) } as GerentesDtoGerente;
    const gerentesMessage = await GerentesProducer.requestService({ operation: "DELETE", data: [targetGerente] });
    res.sendStatus(204);
  } catch (error) {
    res.sendStatus(500);
  }
});

export default router;
