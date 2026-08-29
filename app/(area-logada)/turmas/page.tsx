"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import {
  listarTurmas,
  listarCursos,
  listarLideres,
  type TurmaAdmin,
  type Curso,
  type Lider,
  type StatusTurma,
} from "@/lib/api";
import FormularioNovaTurma from "@/components/FormularioNovaTurma";
import CampoTexto from "@/components/CampoTexto";
import CampoSelecaoUnica, { type OpcaoSelecao } from "@/components/CampoSelecaoUnica";

type FiltroStatus = "ATIVA" | "ENCERRADA" | "TODAS";

const OPCOES_STATUS: OpcaoSelecao<FiltroStatus>[] = [
  { valor: "ATIVA", rotulo: "Ativas" },
  { valor: "ENCERRADA", rotulo: "Encerradas" },
  { valor: "TODAS", rotulo: "Todas" },
];

export default function PaginaTurmas() {
  const [turmas, setTurmas] = useState<TurmaAdmin[] | null>(null);
  const [cursos, setCursos] = useState<Curso[]>([]);
  const [lideres, setLideres] = useState<Lider[]>([]);
  const [erro, setErro] = useState<string | null>(null);
  const [mostrarFormulario, setMostrarFormulario] = useState(false);
  const [filtroStatus, setFiltroStatus] = useState<FiltroStatus>("ATIVA");
  const [busca, setBusca] = useState("");

  async function carregar(status: FiltroStatus) {
    try {
      const [listaTurmas, listaCursos, listaLideres] = await Promise.all([
        listarTurmas(undefined, status === "TODAS" ? undefined : (status as StatusTurma)),
        listarCursos(true),
        listarLideres(),
      ]);
      setTurmas(listaTurmas);
      setCursos(listaCursos);
      setLideres(listaLideres);
    } catch {
      setErro("Não foi possível carregar as turmas.");
    }
  }

  useEffect(() => {
    // eslint-disable-next-line react-hooks/set-state-in-effect -- busca dados sempre que o filtro de status muda
    carregar(filtroStatus);
  }, [filtroStatus]);

  const turmasFiltradas = (turmas ?? []).filter((turma) =>
    turma.nome.toLowerCase().includes(busca.trim().toLowerCase()),
  );

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-xl font-semibold text-neutral-900">Turmas</h1>
        <button
          type="button"
          onClick={() => setMostrarFormulario((atual) => !atual)}
          className="rounded-lg bg-neutral-900 px-4 py-2 text-sm font-medium text-white transition-colors focus:outline-none focus-visible:ring-2 focus-visible:ring-neutral-800"
        >
          {mostrarFormulario ? "Fechar" : "Nova turma"}
        </button>
      </div>

      {mostrarFormulario && (
        <FormularioNovaTurma
          cursos={cursos}
          lideres={lideres}
          onCriada={() => {
            setMostrarFormulario(false);
            carregar(filtroStatus);
          }}
          onCancelar={() => setMostrarFormulario(false)}
        />
      )}

      <div className="flex flex-wrap items-end gap-4">
        <div className="w-full max-w-xs">
          <CampoTexto
            id="busca-turma"
            label="Buscar por nome"
            valor={busca}
            onChange={setBusca}
            placeholder="Nome da turma"
          />
        </div>
        <CampoSelecaoUnica label="Status" opcoes={OPCOES_STATUS} valor={filtroStatus} onChange={setFiltroStatus} />
      </div>

      {erro && (
        <div role="alert" className="rounded-lg border border-red-300 bg-red-50 px-3 py-2 text-sm text-red-700">
          {erro}
        </div>
      )}

      {turmas === null && !erro && <p className="text-sm text-neutral-500">Carregando...</p>}

      {turmas !== null && turmasFiltradas.length === 0 && (
        <p className="text-sm text-neutral-500">Nenhuma turma encontrada.</p>
      )}

      {turmas !== null && turmasFiltradas.length > 0 && (
        <div className="overflow-hidden rounded-xl border border-neutral-200 bg-white">
          <table className="w-full text-left text-sm">
            <thead className="border-b border-neutral-200 bg-neutral-50 text-xs uppercase tracking-wide text-neutral-500">
              <tr>
                <th className="px-4 py-3">Turma</th>
                <th className="px-4 py-3">Curso</th>
                <th className="px-4 py-3">Líder</th>
                <th className="px-4 py-3">Status</th>
                <th className="px-4 py-3"></th>
              </tr>
            </thead>
            <tbody>
              {turmasFiltradas.map((turma) => (
                <tr key={turma.id} className="border-b border-neutral-100 last:border-0">
                  <td className="px-4 py-3 font-medium text-neutral-900">{turma.nome}</td>
                  <td className="px-4 py-3 text-neutral-600">{turma.cursoNome}</td>
                  <td className="px-4 py-3 text-neutral-600">{turma.liderNome ?? "—"}</td>
                  <td className="px-4 py-3">
                    <span
                      className={`rounded-full px-2 py-0.5 text-xs font-medium ${
                        turma.status === "ATIVA" ? "bg-emerald-100 text-emerald-800" : "bg-neutral-100 text-neutral-600"
                      }`}
                    >
                      {turma.status === "ATIVA" ? "Ativa" : "Encerrada"}
                    </span>
                  </td>
                  <td className="px-4 py-3 text-right">
                    <Link href={`/turmas/${turma.id}`} className="text-sm font-medium text-neutral-900 hover:underline">
                      Gerenciar
                    </Link>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
