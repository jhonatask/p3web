"use client";

import Link from "next/link";
import { useEffect, useState } from "react";
import { useSearchParams } from "next/navigation";
import {
  buscarCadastroPorToken,
  solicitarAtualizacaoCadastro,
  ErroApi,
  type ParticipanteCadastrado,
  type ParticipanteEdicao,
} from "@/lib/api";
import CartaoPagina from "./CartaoPagina";
import FormularioCpfChamada from "./FormularioCpfChamada";
import FormularioCadastroBase from "./FormularioCadastroBase";
import CadastroBaseConcluido from "./CadastroBaseConcluido";

type Fase =
  | { tipo: "carregando" }
  | { tipo: "solicitar" }
  | { tipo: "solicitado" }
  | { tipo: "token-invalido" }
  | { tipo: "formulario"; dados: ParticipanteEdicao; token: string }
  | { tipo: "concluido"; nome: string };

export default function AtualizarCadastroBaseCliente() {
  const searchParams = useSearchParams();
  const token = searchParams.get("token");
  const [fase, setFase] = useState<Fase>(token ? { tipo: "carregando" } : { tipo: "solicitar" });

  useEffect(() => {
    if (!token) {
      return;
    }
    buscarCadastroPorToken(token)
      .then((dados) => setFase({ tipo: "formulario", dados, token }))
      .catch(() => setFase({ tipo: "token-invalido" }));
  }, [token]);

  async function tratarCpfEnviado(cpf: string): Promise<string | null> {
    try {
      await solicitarAtualizacaoCadastro(cpf);
      setFase({ tipo: "solicitado" });
      return null;
    } catch (erro) {
      if (erro instanceof ErroApi) {
        return erro.message;
      }
      return "Não foi possível enviar o link de atualização. Verifique sua conexão e tente novamente.";
    }
  }

  function tratarCadastroConcluido(participante: ParticipanteCadastrado) {
    setFase({ tipo: "concluido", nome: participante.nome });
  }

  return (
    <CartaoPagina largura={fase.tipo === "formulario" ? "max-w-2xl" : "max-w-md"}>
      {fase.tipo === "carregando" && <p className="text-center text-sm text-neutral-600">Carregando...</p>}

      {fase.tipo === "solicitar" && (
        <>
          <div className="mb-6">
            <h1 className="text-xl font-semibold text-neutral-900">Atualizar cadastro</h1>
            <p className="mt-2 text-sm text-neutral-600">
              Informe o CPF já cadastrado. Vamos mandar um link de atualização para o e-mail cadastrado.
            </p>
          </div>
          <FormularioCpfChamada onEnviar={tratarCpfEnviado} />
        </>
      )}

      {fase.tipo === "solicitado" && (
        <div className="text-center">
          <h1 className="text-xl font-semibold text-neutral-900">Verifique seu e-mail</h1>
          <p className="mt-3 text-neutral-600">
            Se o CPF estiver cadastrado com um e-mail, enviamos um link para atualizar seus dados.
          </p>
        </div>
      )}

      {fase.tipo === "token-invalido" && (
        <div className="text-center">
          <h1 className="text-xl font-semibold text-neutral-900">Link inválido ou expirado</h1>
          <p className="mt-3 text-neutral-600">Solicite um novo link de atualização de cadastro.</p>
          <Link
            href="/cadastro-base/atualizar"
            className="mt-4 inline-block rounded-lg bg-neutral-900 px-4 py-2 text-sm font-medium text-white transition-colors hover:bg-neutral-800"
          >
            Solicitar novo link
          </Link>
        </div>
      )}

      {fase.tipo === "formulario" && (
        <>
          <div className="mb-6">
            <h1 className="text-xl font-semibold text-neutral-900">Atualizar cadastro</h1>
            <p className="mt-2 text-sm text-neutral-600">Revise seus dados e salve as alterações.</p>
          </div>
          <FormularioCadastroBase
            dadosIniciais={fase.dados}
            tokenAtualizacao={fase.token}
            onCadastroConcluido={tratarCadastroConcluido}
          />
        </>
      )}

      {fase.tipo === "concluido" && <CadastroBaseConcluido nome={fase.nome} />}
    </CartaoPagina>
  );
}
