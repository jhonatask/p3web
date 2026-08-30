"use client";

import { FormEvent, useId, useState } from "react";
import CampoTexto from "./CampoTexto";

type FormularioSolicitarCodigoProps = {
  /** Retorna uma mensagem de erro pra exibir, ou null se deu tudo certo. */
  onEnviar: (email: string) => Promise<string | null>;
};

export default function FormularioSolicitarCodigo({ onEnviar }: FormularioSolicitarCodigoProps) {
  const [email, setEmail] = useState("");
  const [alertaGeral, setAlertaGeral] = useState<string | null>(null);
  const [enviando, setEnviando] = useState(false);
  const idBase = useId();

  async function aoEnviar(evento: FormEvent<HTMLFormElement>) {
    evento.preventDefault();
    setAlertaGeral(null);
    setEnviando(true);
    const mensagemErro = await onEnviar(email);
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
        id={`${idBase}-email`}
        label="E-mail"
        valor={email}
        onChange={setEmail}
        tipo="email"
        autoComplete="username"
        placeholder="voce@familychurch.com"
      />

      <button
        type="submit"
        disabled={enviando}
        className="w-full rounded-lg bg-neutral-900 px-4 py-3 text-base font-medium text-white transition-colors focus:outline-none focus-visible:ring-2 focus-visible:ring-neutral-800 focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:bg-neutral-300"
      >
        {enviando ? "Enviando..." : "Enviar código"}
      </button>
    </form>
  );
}
