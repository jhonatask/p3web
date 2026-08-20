"use client";

import { FormEvent, useId, useState } from "react";
import { useRouter } from "next/navigation";
import { login, ErroApi } from "@/lib/api";
import { salvarSessao } from "@/lib/auth";
import CampoTexto from "./CampoTexto";

export default function FormularioLogin() {
  const router = useRouter();
  const [email, setEmail] = useState("");
  const [senha, setSenha] = useState("");
  const [erro, setErro] = useState<string | null>(null);
  const [enviando, setEnviando] = useState(false);
  const idBase = useId();

  async function aoEnviar(evento: FormEvent<HTMLFormElement>) {
    evento.preventDefault();
    setErro(null);
    setEnviando(true);

    try {
      const resultado = await login(email, senha);
      salvarSessao(resultado.token, resultado.nome);
      router.push("/inicio");
    } catch (erroCapturado) {
      if (erroCapturado instanceof ErroApi) {
        setErro(erroCapturado.message);
      } else {
        setErro("Não foi possível entrar. Verifique sua conexão e tente novamente.");
      }
    } finally {
      setEnviando(false);
    }
  }

  return (
    <form onSubmit={aoEnviar} noValidate className="space-y-4">
      {erro && (
        <div role="alert" className="rounded-lg border border-red-300 bg-red-50 px-3 py-2 text-sm text-red-700">
          {erro}
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

      <CampoTexto
        id={`${idBase}-senha`}
        label="Senha"
        valor={senha}
        onChange={setSenha}
        tipo="password"
        autoComplete="current-password"
      />

      <button
        type="submit"
        disabled={enviando}
        className="w-full rounded-lg bg-neutral-900 px-4 py-3 text-base font-medium text-white transition-colors focus:outline-none focus-visible:ring-2 focus-visible:ring-neutral-800 focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:bg-neutral-300"
      >
        {enviando ? "Entrando..." : "Entrar"}
      </button>
    </form>
  );
}
