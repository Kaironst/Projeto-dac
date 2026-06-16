import { Router, Request, Response } from "express";
import { GerentesDtoGerente } from "../dto/GerentesDto";
import { UsersDtoCliente } from "../dto/UsersDto";
import { gerentesProducer, gerentesProducerCqrs, usersProducer, usersProducerCqrs, emailProducer } from "../messaging/GenericProducerRPC";
import { sagaProducer } from "../messaging/GenericProducer";
import { EmailDto } from "../dto/EmailDto";

const router = Router();

// R09 - Tela inicial do gerente: lista clientes aguardando aprovação (estado = 0)
router.get("/pedidos-aprovacao", async (req: Request, res: Response) => {
  try {
    const clientesMessage = await usersProducerCqrs.requestService({
      operation: "READ_ALL",
      data: [{ id: 0 } as UsersDtoCliente],
      dataType: "cliente"
    });

    // Filtra apenas clientes com estado = 0 (pendentes de aprovação)
    const pendentes = (clientesMessage.data ?? []).filter(
      (c: UsersDtoCliente) => c.estado === 0
    );

    res.status(200).json(pendentes);
  } catch (error) {
    console.error("Erro ao buscar pedidos de aprovação:", error);
    res.sendStatus(500);
  }
});

// R10 - Aprovar cliente: atualiza estado para 1 (ativo) no usersService
router.post("/aprovar-cliente", async (req: Request, res: Response) => {
  try {
    const { cpf, nome, email, telefone, salario } = req.body;

    if (!cpf) {
      return res.status(400).json({ message: "CPF é obrigatório." });
    }

    // Busca o cliente pelo CPF para obter o ID e dados completos
    const clienteMessage = await usersProducerCqrs.requestService({
      operation: "READ_BY_CPF",
      data: [{ cpf } as UsersDtoCliente],
      dataType: "cliente"
    });

    const cliente = clienteMessage.data?.[0];
    if (!cliente) {
      return res.status(404).json({ message: "Cliente não encontrado." });
    }

    // Atualiza estado para 1 (ativo/aprovado)

    const senha:string | undefined = Math.floor(Math.random() * 10000).toString().padStart(4,"0")

    const atualizadoMsg = await usersProducer.requestService({
      operation: "UPDATE",
      data: [{ ...cliente, estado: 1, senha: senha} as UsersDtoCliente],
      dataType: "cliente"
    });

    try {
    const clientesMessage = await usersProducerCqrs.requestService({
      operation: "READ_ALL",
      data: [{ id: 0 } as UsersDtoCliente],
      dataType: "cliente"

    });

    // Filtra apenas clientes com estado = 0 (pendentes de aprovação)
    const pendentes = (clientesMessage.data ?? []).filter(
      (c: UsersDtoCliente) => c.estado === 0
    );

    //manda senha para o email do cliente
    
    await emailProducer.requestService({
      operation: "SEND",
      data: [{assunto:"Aprovação",destinatario:cliente.email,conteudoHtml:`<h1>Sua conta para o banco BANTADS foi aprovada! Sua senha gerada é <b>${senha}</b><h1>`} as EmailDto],
      dataType: "email"
    });
  
    res.status(200).json(pendentes);
  } catch (error) {
    console.error("Erro ao buscar pedidos de aprovação:", error);
    return res.sendStatus(500);
  }
  
    res.status(200).json(atualizadoMsg.data?.[0] ?? { message: `Cliente ${nome} aprovado.` });
  } catch (error) {
    console.error("Erro ao aprovar cliente:", error);
    res.sendStatus(500);
  }
});

// R11 - Rejeitar cliente: atualiza estado para 2 (rejeitado) e registra motivo
router.post("/rejeitar-cliente", async (req: Request, res: Response) => {
  try {
    const { cpf, motivo } = req.body;

    if (!cpf) {
      return res.status(400).json({ message: "CPF é obrigatório." });
    }

    // Busca o cliente pelo CPF para obter o ID
    const clienteMessage = await usersProducerCqrs.requestService({
      operation: "READ_BY_CPF",
      data: [{ cpf } as UsersDtoCliente],
      dataType: "cliente"
    });

    const cliente = clienteMessage.data?.[0];
    if (!cliente) {
      return res.status(404).json({ message: "Cliente não encontrado." });
    }

    // Atualiza estado para 2 (rejeitado)
    await usersProducer.requestService({
      operation: "UPDATE",
      data: [{ ...cliente, estado: 2 } as UsersDtoCliente],
      dataType: "cliente"
    });

    await emailProducer.requestService({
      operation: "SEND",
      data: [{assunto:"Cadastro recusado",destinatario:cliente.email,conteudoHtml:
        `<h1>Parece que o gerente responsável por sua avaliação acabou recusando seu cadastro.</h1><br/>
        <h1>Motivo: <b>${motivo}</b><h1>`} as EmailDto],
      dataType: "email"
    });

    res.status(200).json({ message: `Cliente rejeitado. Motivo: ${motivo}` });
  } catch (error) {
    console.error("Erro ao rejeitar cliente:", error);
    res.sendStatus(500);
  }
});

//GET /:id
router.get("/:id", async (req: Request, res: Response) => {
  try {
    const targetGerente = { id: parseInt(req.params.id) } as GerentesDtoGerente;
    const gerentesMessage = await gerentesProducerCqrs.requestService({ operation: "READ", data: [targetGerente], dataType: "gerente" });
    res.status(200).json(gerentesMessage.data);
  } catch (error) {
    res.sendStatus(500);
  }
});

//GET /email
router.get("/email/:email", async (req: Request, res: Response) => {
  try {
    const targetGerente = { email: req.params.email } as GerentesDtoGerente;
    const gerentesMessage = await gerentesProducerCqrs.requestService({ operation: "READ_BY_EMAIL", data: [targetGerente], dataType: "gerente" });
    res.status(200).json(gerentesMessage.data);
  } catch (error) {
    res.sendStatus(500);
  }
});

//GET /cpf
router.get("/cpf/:cpf", async (req: Request, res: Response) => {
  try {
    const targetGerente = { cpf: req.params.cpf } as GerentesDtoGerente;
    const gerentesMessage = await gerentesProducerCqrs.requestService({ operation: "READ_BY_CPF", data: [targetGerente], dataType: "gerente" });
    res.status(200).json(gerentesMessage.data);
  } catch (error) {
    res.sendStatus(500);
  }
});

router.get("/", async (req: Request, res: Response) => {
  try {
    const targetGerente = { id: 0 } as GerentesDtoGerente;
    const gerentesMessage = await gerentesProducerCqrs.requestService({ operation: "READ_ALL", data: [targetGerente], dataType: "gerente" });
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
      correlationId: null,
      dataType: "gerente"
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
    const gerentesMessage = await gerentesProducer.requestService({ operation: "UPDATE", data: [newGerente], dataType: "gerente" });
    res.status(200).json(gerentesMessage.data);
  } catch (error) {
    res.sendStatus(500);
  }
});

router.delete("/:id", async (req: Request, res: Response) => {
  try {
    const targetGerente = { id: parseInt(req.params.id) } as GerentesDtoGerente;
    const gerentesMessage = await sagaProducer.messageService({
      operation: "REMOVE_GERENTE_START_REMOVER_GERENTE",
      data: [targetGerente.id],
      correlationId: null,
      dataType: "gerente"
    });
    res.sendStatus(204);
  } catch (error) {
    res.sendStatus(500);
  }
});

export default router;
