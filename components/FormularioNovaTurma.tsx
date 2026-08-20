"use client";

import { FormEvent, useId, useState } from "react";
import { criarTurma, ErroApi, type Curso, type TurmaAdmin } from "@/lib/api";
import CampoTexto from "./CampoTexto";
import CampoSelect from "./CampoSelect";

type FormularioNovaTurmaProps = {
  cursos: Curso[];
  onCriada: (turma: TurmaAdmin) => void;
  onCancelar: () => void;
};

export default function FormularioNovaTurma({ cursos, onCriada, onCancelar }: FormularioNovaTurmaProps) {
  const [cursoId, setCursoId] = useState(cursos[0]?.id ?? "");
  const [nome, setNome] = useState("");
  const [moduloAtual, setModuloAtual] = useState("");
  const [erro, setErro] = useState<string | null>(null);
  const [enviando, setEnviando] = useState(false);
  const idBase = useId();

  async function aoEnviar(evento: FormEvent<HTMLFormElement>) {
    evento.preventDefault();
    setErro(null);

    if (!cursoId) {
      setErro("Selecione um curso.");
      return;
    }
    if (nome.trim().length === 0) {
      setErro("Informe o nome da turma.");
      return;
    }

    setEnviando(true);
    try {
      const turma = await criarTurma({ cursoId, nome: nome.trim(), moduloAtual: moduloAtual.trim() || null });
      onCriada(turma);
    } catch (erroCapturado) {
      setErro(erroCapturado instanceof ErroApi ? erroCapturado.message : "Não foi possível criar a turma.");
    } finally {
      setEnviando(false);
    }
  }

  return (
    <form onSubmit={aoEnviar} noValidate className="space-y-4 rounded-xl border border-neutral-200 bg-white p-5">
      {erro && (
        <div role="alert" className="rounded-lg border border-red-300 bg-red-50 px-3 py-2 text-sm text-red-700">
          {erro}
        </div>
      )}

      <CampoSelect
        id={`${idBase}-curso`}
        label="Curso"
        valor={cursoId}
        onChange={setCursoId}
        opcoes={cursos.map((curso) => ({ valor: curso.id, rotulo: curso.nome }))}
        placeholder={cursos.length === 0 ? "Nenhum curso ativo" : undefined}
      />

      <CampoTexto
        id={`${idBase}-nome`}
        label="Nome da turma"
        valor={nome}
        onChange={setNome}
        placeholder="Campus Central - Turma 15"
      />

      <CampoTexto
        id={`${idBase}-modulo`}
        label="Módulo atual"
        valor={moduloAtual}
        onChange={setModuloAtual}
        placeholder="Opcional"
      />

      <div className="flex gap-3">
        <button
          type="submit"
          disabled={enviando}
          className="rounded-lg bg-neutral-900 px-4 py-2 text-sm font-medium text-white transition-colors focus:outline-none focus-visible:ring-2 focus-visible:ring-neutral-800 disabled:cursor-not-allowed disabled:bg-neutral-300"
        >
          {enviando ? "Criando..." : "Criar turma"}
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
