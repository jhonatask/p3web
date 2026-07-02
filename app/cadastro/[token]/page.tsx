import type { Metadata } from "next";
import { buscarTurma, ErroApi, type TurmaPublica } from "@/lib/api";
import CadastroCliente from "@/components/CadastroCliente";
import TurmaInvalida from "@/components/TurmaInvalida";
import TurmaEncerrada from "@/components/TurmaEncerrada";
import ErroCarregamento from "@/components/ErroCarregamento";

export const metadata: Metadata = {
  title: "Cadastro de aluno — Cadastro P3",
  description: "Cadastro público de aluno para controle de presença.",
};

type PaginaCadastroProps = {
  params: Promise<{ token: string }>;
};

type ResultadoCarregamento =
  | { tipo: "sucesso"; turma: TurmaPublica }
  | { tipo: "invalida" }
  | { tipo: "encerrada"; mensagem: string }
  | { tipo: "erro" };

async function carregarTurma(token: string): Promise<ResultadoCarregamento> {
  try {
    const turma = await buscarTurma(token);
    return { tipo: "sucesso", turma };
  } catch (erro) {
    if (erro instanceof ErroApi && erro.status === 404) {
      return { tipo: "invalida" };
    }

    if (erro instanceof ErroApi && erro.status === 409) {
      return { tipo: "encerrada", mensagem: erro.message };
    }

    return { tipo: "erro" };
  }
}

export default async function PaginaCadastro({ params }: PaginaCadastroProps) {
  const { token } = await params;
  const resultado = await carregarTurma(token);

  if (resultado.tipo === "sucesso") {
    return <CadastroCliente token={token} turma={resultado.turma} />;
  }

  if (resultado.tipo === "invalida") {
    return <TurmaInvalida />;
  }

  if (resultado.tipo === "encerrada") {
    return <TurmaEncerrada mensagem={resultado.mensagem} />;
  }

  return <ErroCarregamento />;
}
