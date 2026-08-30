"use client";

import { FormEvent, useId, useState } from "react";
import { somenteDigitos } from "@/lib/texto";
import CampoTexto from "./CampoTexto";

type DadosRedefinicao = {
  codigo: string;
  novaSenha: string;
};

type FormularioRedefinirSenhaProps = {
  /** Retorna uma mensagem de erro pra exibir, ou null se deu tudo certo. */
  onEnviar: (dados: DadosRedefinicao) => Promise<string | null>;
};

export default function FormularioRedefinirSenha({ onEnviar }: FormularioRedefinirSenhaProps) {
  const [codigo, setCodigo] = useState("");
  const [novaSenha, setNovaSenha] = useState("");
  const [confirmarSenha, setConfirmarSenha] = useState("");
  const [erroSenha, setErroSenha] = useState<string | undefined>();
  const [erroConfirmacao, setErroConfirmacao] = useState<string | undefined>();
  const [alertaGeral, setAlertaGeral] = useState<string | null>(null);
  const [enviando, setEnviando] = useState(false);
  const idBase = useId();

  async function aoEnviar(evento: FormEvent<HTMLFormElement>) {
    evento.preventDefault();
    setAlertaGeral(null);

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
    setEnviando(true);
    const mensagemErro = await onEnviar({ codigo: somenteDigitos(codigo), novaSenha });
    setEnviando(false);
    if (mensagemErro) {
      setAlertaGeral(mensagemErro);
    }
  }

  return (
    <form onSubmit={aoEnviar} noValidate className="space-y-4">
      {alertaGeral && (
        <div role="alert" className="rounded-lg border border-red-300 bg-red-50 px-3 py-2 text-sm text-red-700">
          {alertaGeral}
        </div>
      )}

      <CampoTexto
        id={`${idBase}-codigo`}
        label="Código recebido por e-mail"
        valor={codigo}
        onChange={setCodigo}
        inputMode="numeric"
        autoComplete="one-time-code"
        placeholder="000000"
      />

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
        className="w-full rounded-lg bg-neutral-900 px-4 py-3 text-base font-medium text-white transition-colors focus:outline-none focus-visible:ring-2 focus-visible:ring-neutral-800 focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:bg-neutral-300"
      >
        {enviando ? "Salvando..." : "Salvar senha nova"}
      </button>
    </form>
  );
}
