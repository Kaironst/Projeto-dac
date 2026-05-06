import { Router, Request, Response } from "express";
import { GerentesDtoGerente } from "../dto/GerentesDto";
import { gerentesProducer } from "../messaging/GenericProducerRPC";
import { sagaProducer } from "../messaging/GenericProducer";

const router = Router();

//GET /id
router.get("/:id", async (req: Request, res: Response) => {
  try {
    const targetGerente = { id: parseInt(req.params.id) } as GerentesDtoGerente;
    const gerentesMessage = await gerentesProducer.requestService({ operation: "READ", data: [targetGerente] });
    res.status(200).json(gerentesMessage.data);
  } catch (error) {
    res.sendStatus(500);
  }
});

router.get("/", async (req: Request, res: Response) => {
  try {
    const gerentesMessage = await gerentesProducer.requestService({ operation: "READ_ALL", data: null });
    res.status(200).json(gerentesMessage.data);
  } catch (error) {
    res.sendStatus(500);
  }
});

//UTILIZA SAGA: INSERTGERENTE
router.post("/", async (req: Request, res: Response) => {
  try {
    const newGerente = req.body as GerentesDtoGerente;
    console.log("enviando: ", req.body);
    const gerentesMessage = await sagaProducer.messageService({
      operation: "INSERT_GERENTE_START_INSERIR_GERENTE",
      data: [newGerente],
      correlationId: null
    });
    res.sendStatus(202);
  } catch (error) {
    res.sendStatus(500);
  }
});

router.put("/:id", async (req: Request, res: Response) => {
  try {
    const newGerente = req.body as GerentesDtoGerente;
    newGerente.id = parseInt(req.params.id);
    console.log("enviando: ", req.body);
    const gerentesMessage = await gerentesProducer.requestService({ operation: "UPDATE", data: [newGerente] });
    res.status(200).json(gerentesMessage.data);
  } catch (error) {
    res.sendStatus(500);
  }
});

router.delete("/:id", async (req: Request, res: Response) => {
  try {
    const targetGerente = { id: parseInt(req.params.id) } as GerentesDtoGerente;
    const gerentesMessage = await gerentesProducer.requestService({ operation: "DELETE", data: [targetGerente] });
    res.sendStatus(204);
  } catch (error) {
    res.sendStatus(500);
  }
});

export default router;
