"use client";

import { useState } from "react";
import { listarTurmasAbertas, type ParticipanteCadastrado, type TurmaAberta } from "@/lib/api";
import CabecalhoCadastroBase from "./CabecalhoCadastroBase";
import FormularioCadastroBase from "./FormularioCadastroBase";
import CadastroBaseConcluido from "./CadastroBaseConcluido";
import MatricularTurmaAberta from "./MatricularTurmaAberta";
import CartaoPagina from "./CartaoPagina";

type CadastroBaseClienteProps = {
  /**
   * Quando informado, o cadastro concluído não mostra a tela estática de
   * "pode fechar esta página" nem a oferta de matrícula - quem chamou decide
   * o que fazer a seguir (ex.: a chamada segue direto para a avaliação da
   * aula, já matriculado pelo próprio link da turma).
   */
  onConcluido?: (participante: ParticipanteCadastrado) => void;
  /** false quando embutido em uma tela que já tem seu próprio CartaoPagina. */
  comCartao?: boolean;
  cpfInicial?: string;
};

type Fase =
  | { tipo: "formulario" }
  | { tipo: "escolher-turma"; nome: string; cpf: string; tokenMatricula: string; turmas: TurmaAberta[] }
  | { tipo: "concluido"; nome: string; turmaMatriculada?: string };

export default function CadastroBaseCliente({
  onConcluido,
  comCartao = true,
  cpfInicial,
}: CadastroBaseClienteProps) {
  const [fase, setFase] = useState<Fase>({ tipo: "formulario" });

  async function tratarConclusao(participante: ParticipanteCadastrado) {
    if (onConcluido) {
      onConcluido(participante);
      return;
    }

    try {
      const turmas = await listarTurmasAbertas();
      if (turmas.length === 0) {
        setFase({ tipo: "concluido", nome: participante.nome });
        return;
      }
      setFase({
        tipo: "escolher-turma",
        nome: participante.nome,
        cpf: participante.cpf,
        tokenMatricula: participante.tokenMatricula,
        turmas,
      });
    } catch {
      // Se a busca de turmas abertas falhar, o cadastro em si já foi salvo -
      // não vale travar o usuário aqui, só pula a oferta de matrícula.
      setFase({ tipo: "concluido", nome: participante.nome });
    }
  }

  let conteudo: React.ReactNode;
  if (fase.tipo === "concluido") {
    conteudo = <CadastroBaseConcluido nome={fase.nome} turmaMatriculada={fase.turmaMatriculada} />;
  } else if (fase.tipo === "escolher-turma") {
    conteudo = (
      <MatricularTurmaAberta
        cpf={fase.cpf}
        tokenMatricula={fase.tokenMatricula}
        turmas={fase.turmas}
        onMatriculado={(turmaNome) => setFase({ tipo: "concluido", nome: fase.nome, turmaMatriculada: turmaNome })}
        onPular={() => setFase({ tipo: "concluido", nome: fase.nome })}
      />
    );
  } else {
    conteudo = (
      <>
        <CabecalhoCadastroBase />
        <FormularioCadastroBase onCadastroConcluido={tratarConclusao} cpfInicial={cpfInicial} />
      </>
    );
  }

  if (!comCartao) {
    return conteudo;
  }

  return <CartaoPagina largura="max-w-2xl">{conteudo}</CartaoPagina>;
}
