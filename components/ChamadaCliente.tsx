"use client";

import { useState } from "react";
import {
  registrarChamada,
  ErroApi,
  type AulaAtiva,
  type OrigemPresenca,
  type ParticipanteCadastrado,
  type TurmaPublica,
} from "@/lib/api";
import CartaoPagina from "./CartaoPagina";
import CabecalhoChamada from "./CabecalhoChamada";
import FormularioCpfChamada from "./FormularioCpfChamada";
import CadastroBaseCliente from "./CadastroBaseCliente";
import FormularioAvaliacao from "./FormularioAvaliacao";
import ChamadaConcluida from "./ChamadaConcluida";

type Fase =
  | { tipo: "cpf" }
  | { tipo: "cadastro"; cpf: string }
  | { tipo: "confirmando" }
  | { tipo: "avaliacao"; cpf: string; nome: string; origem: OrigemPresenca; turmaOrigemNome: string | null }
  | { tipo: "concluido"; nome: string };

type ChamadaClienteProps = {
  token: string;
  turma: TurmaPublica;
  aulaAtiva: AulaAtiva;
};

export default function ChamadaCliente({ token, turma, aulaAtiva }: ChamadaClienteProps) {
  const [fase, setFase] = useState<Fase>({ tipo: "cpf" });

  async function tratarCpfEnviado(cpf: string): Promise<string | null> {
    try {
      const presenca = await registrarChamada(token, cpf);
      setFase({
        tipo: "avaliacao",
        cpf,
        nome: presenca.participanteNome,
        origem: presenca.origem,
        turmaOrigemNome: presenca.turmaOrigemNome,
      });
      return null;
    } catch (erro) {
      // Sem cadastro na base: abre o cadastro principal antes de liberar a
      // avaliacao - a chamada nunca recadastra, so localiza pelo CPF.
      if (erro instanceof ErroApi && erro.status === 404) {
        setFase({ tipo: "cadastro", cpf });
        return null;
      }
      if (erro instanceof ErroApi) {
        return erro.message;
      }
      return "Não foi possível confirmar sua presença. Verifique sua conexão e tente novamente.";
    }
  }

  async function tratarCadastroConcluido(participante: ParticipanteCadastrado) {
    setFase({ tipo: "confirmando" });
    try {
      const presenca = await registrarChamada(token, participante.cpf);
      setFase({
        tipo: "avaliacao",
        cpf: participante.cpf,
        nome: presenca.participanteNome,
        origem: presenca.origem,
        turmaOrigemNome: presenca.turmaOrigemNome,
      });
    } catch {
      setFase({ tipo: "cpf" });
    }
  }

  const largura = fase.tipo === "cadastro" ? "max-w-2xl" : "max-w-md";

  return (
    <CartaoPagina largura={largura}>
      {fase.tipo === "cpf" && (
        <>
          <CabecalhoChamada turma={turma} aulaAtiva={aulaAtiva} />
          <FormularioCpfChamada onEnviar={tratarCpfEnviado} />
        </>
      )}

      {fase.tipo === "cadastro" && (
        <CadastroBaseCliente comCartao={false} cpfInicial={fase.cpf} onConcluido={tratarCadastroConcluido} />
      )}

      {fase.tipo === "confirmando" && (
        <p className="text-center text-neutral-600">Confirmando sua presença...</p>
      )}

      {fase.tipo === "avaliacao" && (
        <FormularioAvaliacao
          aulaAtiva={aulaAtiva}
          cpf={fase.cpf}
          nome={fase.nome}
          origem={fase.origem}
          turmaOrigemNome={fase.turmaOrigemNome}
          onConcluido={() => setFase({ tipo: "concluido", nome: fase.nome })}
        />
      )}

      {fase.tipo === "concluido" && <ChamadaConcluida nome={fase.nome} />}
    </CartaoPagina>
  );
}
