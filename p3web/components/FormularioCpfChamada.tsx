"use client";

import { FormEvent, useId, useState } from "react";
import { mascararCpf } from "@/lib/masks";
import { validarCpf } from "@/lib/cpf";
import { somenteDigitos } from "@/lib/texto";
import CampoTexto from "./CampoTexto";

type FormularioCpfChamadaProps = {
  /** Retorna uma mensagem de erro pra exibir, ou null se deu tudo certo. */
  onEnviar: (cpf: string) => Promise<string | null>;
};

export default function FormularioCpfChamada({ onEnviar }: FormularioCpfChamadaProps) {
  const [cpf, setCpf] = useState("");
  const [erro, setErro] = useState<string | undefined>();
  const [alertaGeral, setAlertaGeral] = useState<string | null>(null);
  const [enviando, setEnviando] = useState(false);
  const idBase = useId();

  async function aoEnviar(evento: FormEvent<HTMLFormElement>) {
    evento.preventDefault();
    setAlertaGeral(null);

    if (!validarCpf(cpf)) {
      setErro("CPF inválido. Confira os números digitados.");
      return;
    }

    setErro(undefined);
    setEnviando(true);
    const mensagemErro = await onEnviar(somenteDigitos(cpf));
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
        id={`${idBase}-cpf`}
        label="Seu CPF"
        valor={cpf}
        onChange={(valor) => setCpf(mascararCpf(valor))}
        erro={erro}
        inputMode="numeric"
        autoComplete="off"
        placeholder="000.000.000-00"
      />

      <button
        type="submit"
        disabled={enviando}
        className="w-full rounded-lg bg-neutral-900 px-4 py-3 text-base font-medium text-white transition-colors focus:outline-none focus-visible:ring-2 focus-visible:ring-neutral-800 focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:bg-neutral-300"
      >
        {enviando ? "Verificando..." : "Continuar"}
      </button>
    </form>
  );
}
