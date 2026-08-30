"use client";

import Link from "next/link";
import { useState } from "react";
import {
  confirmarCodigoRecuperacao,
  redefinirSenha,
  solicitarCodigoRecuperacao,
  ErroApi,
} from "@/lib/api";
import CartaoPagina from "./CartaoPagina";
import FormularioSolicitarCodigo from "./FormularioSolicitarCodigo";
import FormularioRedefinirSenha from "./FormularioRedefinirSenha";

type Fase = { tipo: "email" } | { tipo: "codigo"; email: string } | { tipo: "concluido" };

export default function RecuperarSenhaCliente() {
  const [fase, setFase] = useState<Fase>({ tipo: "email" });

  async function tratarEmailEnviado(email: string): Promise<string | null> {
    try {
      await solicitarCodigoRecuperacao(email);
      setFase({ tipo: "codigo", email });
      return null;
    } catch (erro) {
      if (erro instanceof ErroApi) {
        return erro.message;
      }
      return "Não foi possível enviar o código. Verifique sua conexão e tente novamente.";
    }
  }

  async function tratarRedefinicaoEnviada(
    email: string,
    dados: { codigo: string; novaSenha: string },
  ): Promise<string | null> {
    try {
      const tokenRecuperacao = await confirmarCodigoRecuperacao(email, dados.codigo);
      await redefinirSenha(tokenRecuperacao, dados.novaSenha);
      setFase({ tipo: "concluido" });
      return null;
    } catch (erro) {
      if (erro instanceof ErroApi) {
        return erro.message;
      }
      return "Não foi possível salvar a senha nova. Verifique sua conexão e tente novamente.";
    }
  }

  return (
    <CartaoPagina>
      {fase.tipo === "email" && (
        <>
          <div className="mb-6 text-center">
            <h1 className="text-xl font-semibold text-neutral-900">Recuperar senha</h1>
            <p className="mt-2 text-sm text-neutral-600">
              Informe seu e-mail. Vamos mandar um código pra você redefinir a senha.
            </p>
          </div>
          <FormularioSolicitarCodigo onEnviar={tratarEmailEnviado} />
        </>
      )}

      {fase.tipo === "codigo" && (
        <>
          <div className="mb-6 text-center">
            <h1 className="text-xl font-semibold text-neutral-900">Digite o código</h1>
            <p className="mt-2 text-sm text-neutral-600">
              Enviamos um código para <span className="font-medium">{fase.email}</span>. Digite o código e
              escolha sua senha nova.
            </p>
          </div>
          <FormularioRedefinirSenha onEnviar={(dados) => tratarRedefinicaoEnviada(fase.email, dados)} />
          <p className="mt-4 text-center text-sm">
            <button
              type="button"
              onClick={() => setFase({ tipo: "email" })}
              className="text-neutral-600 hover:underline"
            >
              Errou o e-mail? Voltar
            </button>
          </p>
        </>
      )}

      {fase.tipo === "concluido" && (
        <div className="text-center">
          <h1 className="text-xl font-semibold text-neutral-900">Senha redefinida!</h1>
          <p className="mt-3 text-neutral-600">Sua senha foi atualizada. Já pode entrar com a senha nova.</p>
          <Link
            href="/login"
            className="mt-4 inline-block rounded-lg bg-neutral-900 px-4 py-2 text-sm font-medium text-white transition-colors hover:bg-neutral-800"
          >
            Ir para o login
          </Link>
        </div>
      )}
    </CartaoPagina>
  );
}
