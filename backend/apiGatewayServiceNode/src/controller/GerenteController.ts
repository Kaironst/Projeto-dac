import { Router, Request, Response } from "express";
import { GerentesDtoGerente } from "../dto/GerentesDto";
import { AutocadastroAprovacao } from "../dto/AutocadastroDto";
import { autocadastroUsersProducer, gerentesProducer } from "../messaging/GenericProducerRPC";
import { sagaProducer } from "../messaging/GenericProducer";

const router = Router();

const parseOptionalNumber = (value: unknown): number | null => {
  if (value === null || value === undefined || value === "") {
    return null;
  }

  const parsed = Number(value);
  return Number.isNaN(parsed) ? null : parsed;
};

router.get("/pedidos-aprovacao", async (req: Request, res: Response) => {
  try {
    const pedidosMessage = await autocadastroUsersProducer.requestService({
      operation: "READ_AUTOCADASTRO_PENDENTES",
      data: null
    });

    if (pedidosMessage.operation !== "RESULT") {
      res.sendStatus(502);
      return;
    }

    const pedidos = (pedidosMessage.data ?? []).map((solicitacao) => ({
      id: solicitacao.id,
      solicitacaoId: solicitacao.id,
      cpf: solicitacao.cliente?.cpf ?? null,
      nome: solicitacao.cliente?.nome ?? null,
      salario: solicitacao.cliente?.salario ?? null,
      email: solicitacao.cliente?.email ?? null,
      telefone: solicitacao.cliente?.telefone ?? null,
      gerenteId: solicitacao.gerente?.id ?? null
    }));

    res.status(200).json(pedidos);
  } catch (error) {
    res.sendStatus(500);
  }
});

router.post("/aprovar-cliente", async (req: Request, res: Response) => {
  try {
    const solicitacaoId = parseOptionalNumber(req.body.solicitacaoId ?? req.body.id);
    const gerenteId = parseOptionalNumber(req.body.gerenteId);
    const cpf = typeof req.body.cpf === "string" ? req.body.cpf : null;

    if (solicitacaoId === null && cpf === null) {
      res.status(400).json({ message: "Informe solicitacaoId ou cpf para aprovar." });
      return;
    }

    const aprovacao: AutocadastroAprovacao = {
      solicitacaoId,
      cpf,
      gerenteId
    };

    await sagaProducer.messageService({
      operation: "AUTOCADASTRO_APROVAR_SOLICITACAO",
      data: [aprovacao],
      correlationId: null
    });

    res.sendStatus(202);
  } catch (error) {
    res.sendStatus(500);
  }
});

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
