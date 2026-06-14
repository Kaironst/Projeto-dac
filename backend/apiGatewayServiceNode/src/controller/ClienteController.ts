import { Router, Request, Response } from "express";
import { UsersDtoCliente } from "../dto/UsersDto";
import { usersProducer, usersProducerCqrs } from "../messaging/GenericProducerRPC";
import { sagaProducer } from "../messaging/GenericProducer";

const router = Router();
const CPF_DUPLICADO_ERROR = "ERROR_CPF_DUPLICADO";
const CPF_DUPLICADO_MESSAGE = "CPF já cadastrado ou aguardando aprovação.";

//GET /id
router.get("/:id", async (req: Request, res: Response) => {
  try {
    const targetCliente = { id: parseInt(req.params.id) } as UsersDtoCliente;
    const clientesMessage = await usersProducerCqrs.requestService({ operation: "READ", data: [targetCliente], dataType: "cliente" });
    res.status(200).json(clientesMessage.data);
  } catch (error) {
    res.sendStatus(500);
  }
});

//GET /email
router.get("/email/:email", async (req: Request, res: Response) => {
  try {
    const targetCliente = { email: req.params.email } as UsersDtoCliente;
    const clientesMessage = await usersProducerCqrs.requestService({ operation: "READ_BY_EMAIL", data: [targetCliente], dataType: "cliente" });
    res.status(200).json(clientesMessage.data);
  } catch (error) {
    res.sendStatus(500);
  }
});

//GET /cpf
router.get("/cpf/:cpf", async (req: Request, res: Response) => {
  try {
    const targetCliente = { cpf: req.params.cpf } as UsersDtoCliente;
    const clientesMessage = await usersProducerCqrs.requestService({ operation: "READ_BY_CPF", data: [targetCliente], dataType: "cliente" });
    res.status(200).json(clientesMessage.data);
  } catch (error) {
    res.sendStatus(500);
  }
});

router.get("/", async (req: Request, res: Response) => {
  try {
    const targetCliente = { id: 0 } as UsersDtoCliente;
    const clientesMessage = await usersProducerCqrs.requestService({ operation: "READ_ALL", data: [targetCliente], dataType: "cliente" });
    res.status(200).json(clientesMessage.data);
  } catch (error) {
    res.sendStatus(500);
  }
});

//UTILIZA SAGA AUTOCADASTRO
router.post("/", async (req: Request, res: Response) => {
  try {
    const newCliente = req.body as UsersDtoCliente;

    console.log("enviando: ", req.body);

    const clienteComCpf = await usersProducerCqrs.requestService({ operation: "READ_BY_CPF", data: [newCliente], dataType: "cliente" });
    if (clienteComCpf.data?.length !== 0) {
      return res.status(409).json({
        message: CPF_DUPLICADO_MESSAGE
      });
    };

    const clientesMessage = await sagaProducer.messageService({ operation: "AUTOCADASTRO_START_AUTOCADASTRO", data: [newCliente], dataType: "cliente", correlationId: null });
    return res.status(202);

  } catch (error) {
    return res.status(500).json({
      message: "Erro ao criar cliente."
    });
  }
});

// UTILIZA SAGA, ATUALIZAR LIMITE
router.put("/:id", async (req: Request, res: Response) => {
  try {
    const newCliente = req.body as UsersDtoCliente;
    newCliente.id = parseInt(req.params.id);
    console.log("enviando: ", req.body);
    const clientesMessage = await sagaProducer.messageService({
      operation: "ATUALIZAR_LIMITE_START_ATUALIZAR_LIMITE",
      data: [newCliente],
      dataType: "cliente",
      correlationId: null
    });
    res.status(202);
  } catch (error) {
    res.sendStatus(500);
  }
});

router.delete("/:id", async (req: Request, res: Response) => {
  try {
    const targetCliente = { id: parseInt(req.params.id) } as UsersDtoCliente;
    const clientesMessage = await usersProducer.requestService({ operation: "DELETE", data: [targetCliente], dataType: "cliente" });
    res.sendStatus(204);
  } catch (error) {
    res.sendStatus(500);
  }
});

export default router;
