import { Router, Request, Response } from "express";
import { UsersDtoCliente } from "../dto/UsersDto";
import { usersProducer } from "../messaging/GenericProducerRPC";
import { sagaProducer } from "../messaging/GenericProducer";
import { AutocadastroSolicitacaoEntrada } from "../dto/AutocadastroDto";

const router = Router();
const CPF_DUPLICADO_ERROR = "ERROR_CPF_DUPLICADO";
const CPF_DUPLICADO_MESSAGE = "CPF já cadastrado ou aguardando aprovação.";

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
    const clientesMessage = await usersProducer.requestService({
      operation: "VALIDATE_AUTOCADASTRO_CPF",
      data: [newCliente]
    });

    if (clientesMessage?.operation === CPF_DUPLICADO_ERROR) {
      return res.status(409).json({
        message: CPF_DUPLICADO_MESSAGE
      });
    }

    if (clientesMessage?.operation !== "RESULT") {
      return res.status(500).json({
        message: "Erro ao criar cliente."
      });
    }

    const solicitacao: AutocadastroSolicitacaoEntrada = { cliente: newCliente };
    await sagaProducer.messageService({
      operation: "AUTOCADASTRO_START",
      data: [solicitacao],
      correlationId: null
    });

    return res.status(202).json({
      message: "Solicitação de autocadastro enviada para aprovação."
    });
  } catch (error) {
    return res.status(500).json({
      message: "Erro ao criar cliente."
    });
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
