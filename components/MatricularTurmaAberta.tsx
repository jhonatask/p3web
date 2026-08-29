"use client";

import { useState } from "react";
import { matricularEmTurma, ErroApi, type TurmaAberta } from "@/lib/api";

type MatricularTurmaAbertaProps = {
  cpf: string;
  tokenMatricula: string;
  turmas: TurmaAberta[];
  onMatriculado: (turmaNome: string) => void;
  onPular: () => void;
};

export default function MatricularTurmaAberta({
  cpf,
  tokenMatricula,
  turmas,
  onMatriculado,
  onPular,
}: MatricularTurmaAbertaProps) {
  const [matriculando, setMatriculando] = useState<string | null>(null);
  const [erro, setErro] = useState<string | null>(null);

  async function matricular(turma: TurmaAberta) {
    setErro(null);
    setMatriculando(turma.id);
    try {
      const resultado = await matricularEmTurma(turma.id, cpf, tokenMatricula);
      onMatriculado(resultado.turmaNome);
    } catch (erroCapturado) {
      setErro(erroCapturado instanceof ErroApi ? erroCapturado.message : "Não foi possível concluir a matrícula.");
    } finally {
      setMatriculando(null);
    }
  }

  return (
    <div>
      <h1 className="text-xl font-semibold text-neutral-900">Quer se matricular em uma turma?</h1>
      <p className="mt-2 text-sm text-neutral-600">
        Estas turmas ainda não começaram e estão abertas para matrícula.
      </p>

      {erro && (
        <div role="alert" className="mt-4 rounded-lg border border-red-300 bg-red-50 px-3 py-2 text-sm text-red-700">
          {erro}
        </div>
      )}

      <ul className="mt-4 space-y-3">
        {turmas.map((turma) => (
          <li
            key={turma.id}
            className="flex flex-wrap items-center justify-between gap-3 rounded-lg border border-neutral-200 px-4 py-3"
          >
            <div>
              <p className="font-medium text-neutral-900">{turma.cursoNome}</p>
              <p className="text-sm text-neutral-600">{turma.nome}</p>
            </div>
            <button
              type="button"
              onClick={() => matricular(turma)}
              disabled={matriculando !== null}
              className="rounded-lg bg-neutral-900 px-4 py-2 text-sm font-medium text-white transition-colors hover:bg-neutral-800 disabled:cursor-not-allowed disabled:bg-neutral-300"
            >
              {matriculando === turma.id ? "Matriculando..." : "Matricular"}
            </button>
          </li>
        ))}
      </ul>

      <button
        type="button"
        onClick={onPular}
        disabled={matriculando !== null}
        className="mt-4 w-full text-center text-sm text-neutral-500 hover:underline disabled:cursor-not-allowed"
      >
        Agora não
      </button>
    </div>
  );
}
