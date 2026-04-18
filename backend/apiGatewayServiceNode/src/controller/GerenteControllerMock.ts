import { Router, Request, Response } from "express";

const router = Router();

interface GerenteCadastro {
  nome: string;
  cpf: string;
  email: string;
  telefone: string;
  senha: string;
}

const gerentesMock: GerenteCadastro[] = [
  {
    cpf: "98574307084",
    nome: "Genieve",
    email: "ger1@bantads.com.br",
    telefone: "",
    senha: "tads"
  },
  {
    cpf: "64065268052",
    nome: "Godophredo",
    email: "ger2@bantads.com.br",
    telefone: "",
    senha: "tads"
  },
  {
    cpf: "23862179060",
    nome: "Gyandula",
    email: "ger3@bantads.com.br",
    telefone: "",
    senha: "tads"
  }
];

router.get("/", async (_req: Request, res: Response) => {
  try {
    res.status(200).json(gerentesMock);
  } catch (_error) {
    res.sendStatus(500);
  }
});

export default router;
