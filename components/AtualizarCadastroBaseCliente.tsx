"use client";

import Link from "next/link";
import { useState } from "react";
import { buscarCadastroPorCpf, ErroApi, type ParticipanteCadastrado, type ParticipanteEdicao } from "@/lib/api";
import CartaoPagina from "./CartaoPagina";
import FormularioCpfChamada from "./FormularioCpfChamada";
import FormularioCadastroBase from "./FormularioCadastroBase";
import CadastroBaseConcluido from "./CadastroBaseConcluido";

type Fase =
  | { tipo: "cpf" }
  | { tipo: "nao-encontrado"; cpf: string }
  | { tipo: "formulario"; dados: ParticipanteEdicao }
  | { tipo: "concluido"; nome: string };

export default function AtualizarCadastroBaseCliente() {
  const [fase, setFase] = useState<Fase>({ tipo: "cpf" });

  async function tratarCpfEnviado(cpf: string): Promise<string | null> {
    try {
      const dados = await buscarCadastroPorCpf(cpf);
      setFase({ tipo: "formulario", dados });
      return null;
    } catch (erro) {
      if (erro instanceof ErroApi && erro.status === 404) {
        setFase({ tipo: "nao-encontrado", cpf });
        return null;
      }
      if (erro instanceof ErroApi) {
        return erro.message;
      }
      return "Não foi possível buscar seu cadastro. Verifique sua conexão e tente novamente.";
    }
  }

  function tratarCadastroConcluido(participante: ParticipanteCadastrado) {
    setFase({ tipo: "concluido", nome: participante.nome });
  }

  return (
    <CartaoPagina largura={fase.tipo === "formulario" ? "max-w-2xl" : "max-w-md"}>
      {fase.tipo === "cpf" && (
        <>
          <div className="mb-6">
            <h1 className="text-xl font-semibold text-neutral-900">Atualizar cadastro</h1>
            <p className="mt-2 text-sm text-neutral-600">
              Informe o CPF já cadastrado para revisar e atualizar seus dados.
            </p>
          </div>
          <FormularioCpfChamada onEnviar={tratarCpfEnviado} />
        </>
      )}

      {fase.tipo === "nao-encontrado" && (
        <div className="text-center">
          <h1 className="text-xl font-semibold text-neutral-900">CPF não encontrado</h1>
          <p className="mt-3 text-neutral-600">
            Não encontramos nenhum cadastro com esse CPF. Faça o cadastro inicial antes de atualizar os dados.
          </p>
          <Link
            href="/cadastro-base"
            className="mt-4 inline-block rounded-lg bg-neutral-900 px-4 py-2 text-sm font-medium text-white transition-colors hover:bg-neutral-800"
          >
            Fazer cadastro
          </Link>
        </div>
      )}

      {fase.tipo === "formulario" && (
        <>
          <div className="mb-6">
            <h1 className="text-xl font-semibold text-neutral-900">Atualizar cadastro</h1>
            <p className="mt-2 text-sm text-neutral-600">Revise seus dados e salve as alterações.</p>
          </div>
          <FormularioCadastroBase dadosIniciais={fase.dados} onCadastroConcluido={tratarCadastroConcluido} />
        </>
      )}

      {fase.tipo === "concluido" && <CadastroBaseConcluido nome={fase.nome} />}
    </CartaoPagina>
  );
}
