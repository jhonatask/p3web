"use client";

import { FormEvent, useId, useState } from "react";
import { atualizarTurma, ErroApi, type Campus, type TurmaAdmin } from "@/lib/api";
import CampoTexto from "./CampoTexto";
import CampoSelect from "./CampoSelect";

type FormularioEditarTurmaProps = {
  turma: TurmaAdmin;
  campi: Campus[];
  onAtualizada: (turma: TurmaAdmin) => void;
  onCancelar: () => void;
};

export default function FormularioEditarTurma({ turma, campi, onAtualizada, onCancelar }: FormularioEditarTurmaProps) {
  const [nome, setNome] = useState(turma.nome);
  const [moduloAtual, setModuloAtual] = useState(turma.moduloAtual ?? "");
  const [campusId, setCampusId] = useState(turma.campusId ?? campi[0]?.id ?? "");
  const [erro, setErro] = useState<string | null>(null);
  const [enviando, setEnviando] = useState(false);
  const idBase = useId();

  async function aoEnviar(evento: FormEvent<HTMLFormElement>) {
    evento.preventDefault();
    setErro(null);

    if (!campusId) {
      setErro("Selecione um campus.");
      return;
    }
    if (nome.trim().length === 0) {
      setErro("Informe o nome da turma.");
      return;
    }

    setEnviando(true);
    try {
      const turmaAtualizada = await atualizarTurma(turma.id, {
        campusId,
        nome: nome.trim(),
        moduloAtual: moduloAtual.trim() || null,
      });
      onAtualizada(turmaAtualizada);
    } catch (erroCapturado) {
      setErro(erroCapturado instanceof ErroApi ? erroCapturado.message : "Não foi possível salvar a turma.");
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

      <CampoTexto id={`${idBase}-nome`} label="Nome da turma" valor={nome} onChange={setNome} />

      <CampoSelect
        id={`${idBase}-campus`}
        label="Campus"
        valor={campusId}
        onChange={setCampusId}
        opcoes={campi.map((campus) => ({ valor: campus.id, rotulo: campus.nome }))}
        placeholder={campi.length === 0 ? "Nenhum campus ativo" : undefined}
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
          {enviando ? "Salvando..." : "Salvar"}
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
