"use client";

import { FormEvent, useId, useState } from "react";
import { concederLiderTurma, ErroApi, type TurmaAdmin } from "@/lib/api";
import CampoTexto from "./CampoTexto";

type FormularioLiderTurmaProps = {
  turma: TurmaAdmin;
  onDefinido: () => void;
  onCancelar: () => void;
};

export default function FormularioLiderTurma({ turma, onDefinido, onCancelar }: FormularioLiderTurmaProps) {
  const [nome, setNome] = useState("");
  const [email, setEmail] = useState("");
  const [erro, setErro] = useState<string | null>(null);
  const [enviando, setEnviando] = useState(false);
  const idBase = useId();

  async function aoEnviar(evento: FormEvent<HTMLFormElement>) {
    evento.preventDefault();
    setErro(null);

    if (nome.trim().length === 0 || email.trim().length === 0) {
      setErro("Informe o nome e o e-mail do líder.");
      return;
    }

    setEnviando(true);
    try {
      await concederLiderTurma(turma.id, { nome: nome.trim(), email: email.trim() });
      onDefinido();
    } catch (erroCapturado) {
      setErro(erroCapturado instanceof ErroApi ? erroCapturado.message : "Não foi possível definir o líder.");
    } finally {
      setEnviando(false);
    }
  }

  return (
    <form onSubmit={aoEnviar} noValidate className="space-y-4 rounded-xl border border-neutral-200 bg-white p-5">
      <p className="text-sm text-neutral-600">
        Definir líder de <span className="font-medium text-neutral-900">{turma.nome}</span>
      </p>

      {erro && (
        <div role="alert" className="rounded-lg border border-red-300 bg-red-50 px-3 py-2 text-sm text-red-700">
          {erro}
        </div>
      )}

      <CampoTexto
        id={`${idBase}-nome`}
        label="Nome do líder"
        valor={nome}
        onChange={setNome}
        placeholder="Antônio Ferreira"
      />

      <CampoTexto
        id={`${idBase}-email`}
        label="E-mail do líder"
        valor={email}
        onChange={setEmail}
        tipo="email"
        autoComplete="email"
        placeholder="lider@example.com"
      />

      <div className="flex gap-3">
        <button
          type="submit"
          disabled={enviando}
          className="rounded-lg bg-neutral-900 px-4 py-2 text-sm font-medium text-white transition-colors focus:outline-none focus-visible:ring-2 focus-visible:ring-neutral-800 disabled:cursor-not-allowed disabled:bg-neutral-300"
        >
          {enviando ? "Salvando..." : "Definir líder"}
        </button>
        <button
          type="button"
          onClick={onCancelar}
          className="rounded-lg border border-neutral-300 px-4 py-2 text-sm text-neutral-700 transition-colors hover:border-neutral-400"
        >
          Cancelar
        </button>
      </div>
    </form>
  );
}
