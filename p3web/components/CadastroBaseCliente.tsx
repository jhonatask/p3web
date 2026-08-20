"use client";

import { useState } from "react";
import type { ParticipanteCadastrado } from "@/lib/api";
import CabecalhoCadastroBase from "./CabecalhoCadastroBase";
import FormularioCadastroBase from "./FormularioCadastroBase";
import CadastroBaseConcluido from "./CadastroBaseConcluido";
import CartaoPagina from "./CartaoPagina";

type CadastroBaseClienteProps = {
  /**
   * Quando informado, o cadastro concluído não mostra a tela estática de
   * "pode fechar esta página" - quem chamou decide o que fazer a seguir
   * (ex.: a chamada segue direto para a avaliação da aula).
   */
  onConcluido?: (participante: ParticipanteCadastrado) => void;
  /** false quando embutido em uma tela que já tem seu próprio CartaoPagina. */
  comCartao?: boolean;
  cpfInicial?: string;
};

export default function CadastroBaseCliente({
  onConcluido,
  comCartao = true,
  cpfInicial,
}: CadastroBaseClienteProps) {
  const [participanteCadastrado, setParticipanteCadastrado] = useState<ParticipanteCadastrado | null>(null);

  function tratarConclusao(participante: ParticipanteCadastrado) {
    if (onConcluido) {
      onConcluido(participante);
      return;
    }
    setParticipanteCadastrado(participante);
  }

  const conteudo = participanteCadastrado ? (
    <CadastroBaseConcluido nome={participanteCadastrado.nome} />
  ) : (
    <>
      <CabecalhoCadastroBase />
      <FormularioCadastroBase onCadastroConcluido={tratarConclusao} cpfInicial={cpfInicial} />
    </>
  );

  if (!comCartao) {
    return conteudo;
  }

  return <CartaoPagina largura="max-w-2xl">{conteudo}</CartaoPagina>;
}
