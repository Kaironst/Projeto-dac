import { Router, Request, Response } from "express";
import { ContasDtoConta } from "../dto/ContasDto";
import { ConsultaClienteDto } from "../dto/ConsultaClienteDto";
import { UsersDtoCliente, UsersDtoEndereco } from "../dto/UsersDto";
import { contasProducer, usersProducer } from "../messaging/GenericProducerRPC";

const router = Router();

async function carregarClientesComContas(): Promise<ConsultaClienteDto[]> {
  const [clientesMessage, contasMessage] = await Promise.all([
    usersProducer.requestService({ operation: "READ_ALL", data: null }),
    contasProducer.requestService({ operation: "READ_ALL", data: null }),
  ]);

  if (clientesMessage.operation !== "RESULT" || contasMessage.operation !== "RESULT") {
    throw new Error("Erro ao consultar clientes ou contas.");
  }

  const clientes = clientesMessage.data ?? [];
  const contas = contasMessage.data ?? [];

  return clientes.map((cliente) => montarClienteConsulta(cliente, contas));
}

function montarClienteConsulta(cliente: UsersDtoCliente, contas: ContasDtoConta[]): ConsultaClienteDto {
  const conta = contas.find((contaAtual) => contaAtual.cliente?.id === cliente.id) ?? null;
  const endereco = primeiroEndereco(cliente.enderecos);

  return {
    id: cliente.id,
    nome: cliente.nome ?? "",
    email: cliente.email ?? "",
    cpf: cliente.cpf ?? "",
    telefone: cliente.telefone ?? "",
    salario: cliente.salario ?? 0,
    endereco: {
      cep: endereco?.cep ?? "",
      logradouro: endereco?.logradouro ?? "",
      numero: endereco?.numero != null ? String(endereco.numero) : "",
      complemento: endereco?.complemento ?? "",
      cidade: endereco?.cidade ?? "",
      estado: endereco?.estado ?? "",
    },
    conta: {
      id: conta?.id ?? null,
      numero: conta?.numero ?? "",
      saldo: conta?.saldo ?? 0,
      limite: conta?.limite ?? 0,
      gerenteId: conta?.gerente?.id ?? null,
      dataCriacao: conta?.dataCriacao ?? null,
    },
  };
}

function primeiroEndereco(enderecos: null | UsersDtoEndereco[] | undefined): UsersDtoEndereco | null {
  return enderecos?.[0] ?? null;
}

function normalizarCpf(cpf: string): string {
  return cpf.replace(/\D/g, "");
}

router.get("/clientes/top-saldo", async (req: Request, res: Response) => {
  try {
    const limitParam = Number(req.query.limit ?? 3);
    const limit = Number.isFinite(limitParam) && limitParam > 0 ? limitParam : 3;
    const clientes = await carregarClientesComContas();

    return res.status(200).json(
      clientes
        .sort((a, b) => b.conta.saldo - a.conta.saldo)
        .slice(0, limit)
    );
  } catch (error) {
    console.error("Erro ao consultar top clientes:", error);
    return res.sendStatus(500);
  }
});

router.get("/clientes/cpf/:cpf", async (req: Request, res: Response) => {
  try {
    const cpfBuscado = normalizarCpf(req.params.cpf);
    const clientes = await carregarClientesComContas();
    const cliente = clientes.find((clienteAtual) => normalizarCpf(clienteAtual.cpf) === cpfBuscado);

    if (!cliente) {
      return res.sendStatus(404);
    }

    return res.status(200).json(cliente);
  } catch (error) {
    console.error("Erro ao consultar cliente por CPF:", error);
    return res.sendStatus(500);
  }
});

router.get("/clientes", async (_req: Request, res: Response) => {
  try {
    const clientes = await carregarClientesComContas();
    return res.status(200).json(clientes);
  } catch (error) {
    console.error("Erro ao consultar clientes:", error);
    return res.sendStatus(500);
  }
});

export default router;
