"use client";

import { FormEvent, useId, useState } from "react";
import { redefinirSenha, ErroApi } from "@/lib/api";
import { obterToken } from "@/lib/auth";
import CampoTexto from "./CampoTexto";

export default function FormularioTrocarSenha() {
  const [novaSenha, setNovaSenha] = useState("");
  const [confirmarSenha, setConfirmarSenha] = useState("");
  const [erroSenha, setErroSenha] = useState<string | undefined>();
  const [erroConfirmacao, setErroConfirmacao] = useState<string | undefined>();
  const [alertaGeral, setAlertaGeral] = useState<string | null>(null);
  const [sucesso, setSucesso] = useState(false);
  const [enviando, setEnviando] = useState(false);
  const idBase = useId();

  async function aoEnviar(evento: FormEvent<HTMLFormElement>) {
    evento.preventDefault();
    setAlertaGeral(null);
    setSucesso(false);

    if (novaSenha.length < 8) {
      setErroSenha("A senha precisa ter no mínimo 8 caracteres.");
      return;
    }
    setErroSenha(undefined);

    if (novaSenha !== confirmarSenha) {
      setErroConfirmacao("As senhas não conferem.");
      return;
    }
    setErroConfirmacao(undefined);

    const token = obterToken();
    if (!token) {
      setAlertaGeral("Sessão expirada. Faça login novamente.");
      return;
    }

    setEnviando(true);
    try {
      await redefinirSenha(token, novaSenha);
      setNovaSenha("");
      setConfirmarSenha("");
      setSucesso(true);
    } catch (erro) {
      if (erro instanceof ErroApi) {
        setAlertaGeral(erro.message);
      } else {
        setAlertaGeral("Não foi possível trocar a senha. Verifique sua conexão e tente novamente.");
      }
    } finally {
      setEnviando(false);
    }
  }

  return (
    <form onSubmit={aoEnviar} noValidate className="max-w-sm space-y-4">
      {alertaGeral && (
        <div role="alert" className="rounded-lg border border-red-300 bg-red-50 px-3 py-2 text-sm text-red-700">
          {alertaGeral}
        </div>
      )}

      {sucesso && (
        <div className="rounded-lg bg-emerald-50 px-3 py-2 text-sm text-emerald-800">Senha atualizada.</div>
      )}

      <CampoTexto
        id={`${idBase}-nova-senha`}
        label="Senha nova"
        valor={novaSenha}
        onChange={setNovaSenha}
        tipo="password"
        autoComplete="new-password"
        erro={erroSenha}
      />

      <CampoTexto
        id={`${idBase}-confirmar-senha`}
        label="Confirmar senha nova"
        valor={confirmarSenha}
        onChange={setConfirmarSenha}
        tipo="password"
        autoComplete="new-password"
        erro={erroConfirmacao}
      />

      <button
        type="submit"
        disabled={enviando}
        className="rounded-lg bg-neutral-900 px-4 py-2 text-sm font-medium text-white transition-colors focus:outline-none focus-visible:ring-2 focus-visible:ring-neutral-800 focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:bg-neutral-300"
      >
        {enviando ? "Salvando..." : "Salvar senha nova"}
      </button>
    </form>
  );
}
