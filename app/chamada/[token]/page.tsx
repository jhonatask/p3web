import type { Metadata } from "next";
import { buscarAulaAtiva, buscarTurma, ErroApi, type AulaAtiva, type TurmaPublica } from "@/lib/api";
import ChamadaCliente from "@/components/ChamadaCliente";
import TurmaInvalida from "@/components/TurmaInvalida";
import TurmaEncerrada from "@/components/TurmaEncerrada";
import SemAulaAtiva from "@/components/SemAulaAtiva";
import ErroCarregamento from "@/components/ErroCarregamento";

export const metadata: Metadata = {
  title: "Chamada — P3",
  description: "Chamada e avaliação da aula da Escola de Hombridade.",
};

type PaginaChamadaProps = {
  params: Promise<{ token: string }>;
};

type ResultadoCarregamento =
  | { tipo: "sucesso"; turma: TurmaPublica; aulaAtiva: AulaAtiva }
  | { tipo: "invalida" }
  | { tipo: "encerrada"; mensagem: string }
  | { tipo: "sem-aula-ativa" }
  | { tipo: "erro" };

async function carregar(token: string): Promise<ResultadoCarregamento> {
  let turma: TurmaPublica;
  try {
    turma = await buscarTurma(token);
  } catch (erro) {
    if (erro instanceof ErroApi && erro.status === 404) {
      return { tipo: "invalida" };
    }
    if (erro instanceof ErroApi && erro.status === 409) {
      return { tipo: "encerrada", mensagem: erro.message };
    }
    return { tipo: "erro" };
  }

  try {
    const aulaAtiva = await buscarAulaAtiva(token);
    return { tipo: "sucesso", turma, aulaAtiva };
  } catch (erro) {
    if (erro instanceof ErroApi && (erro.status === 404 || erro.status === 409)) {
      return { tipo: "sem-aula-ativa" };
    }
    return { tipo: "erro" };
  }
}

export default async function PaginaChamada({ params }: PaginaChamadaProps) {
  const { token } = await params;
  const resultado = await carregar(token);

  if (resultado.tipo === "sucesso") {
    return <ChamadaCliente token={token} turma={resultado.turma} aulaAtiva={resultado.aulaAtiva} />;
  }

  if (resultado.tipo === "invalida") {
    return <TurmaInvalida />;
  }

  if (resultado.tipo === "encerrada") {
    return <TurmaEncerrada mensagem={resultado.mensagem} />;
  }

  if (resultado.tipo === "sem-aula-ativa") {
    return <SemAulaAtiva />;
  }

  return <ErroCarregamento />;
}
