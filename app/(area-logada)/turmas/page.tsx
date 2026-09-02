"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import {
  listarTurmas,
  listarCursos,
  listarCampus,
  obterMe,
  type TurmaAdmin,
  type Curso,
  type Campus,
  type StatusTurma,
} from "@/lib/api";
import FormularioNovaTurma from "@/components/FormularioNovaTurma";
import FormularioLiderTurma from "@/components/FormularioLiderTurma";
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
  const [campi, setCampi] = useState<Campus[]>([]);
  const [erro, setErro] = useState<string | null>(null);
  const [mostrarFormulario, setMostrarFormulario] = useState(false);
  const [filtroStatus, setFiltroStatus] = useState<FiltroStatus>("ATIVA");
  const [busca, setBusca] = useState("");
  const [podeDelegarLiderP3, setPodeDelegarLiderP3] = useState(false);
  const [turmaParaLider, setTurmaParaLider] = useState<TurmaAdmin | null>(null);

  async function carregar(status: FiltroStatus) {
    try {
      const [listaTurmas, listaCursos, listaCampi] = await Promise.all([
        listarTurmas(undefined, status === "TODAS" ? undefined : (status as StatusTurma)),
        listarCursos(true),
        listarCampus(true),
      ]);
      setTurmas(listaTurmas);
      setCursos(listaCursos);
      setCampi(listaCampi);
    } catch {
      setErro("Não foi possível carregar as turmas.");
    }
  }

  useEffect(() => {
    // eslint-disable-next-line react-hooks/set-state-in-effect -- busca dados sempre que o filtro de status muda
    carregar(filtroStatus);
  }, [filtroStatus]);

  useEffect(() => {
    obterMe()
      .then((me) => setPodeDelegarLiderP3(me.podeDelegarLiderP3))
      .catch(() => setPodeDelegarLiderP3(false));
  }, []);

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
          campi={campi}
          onCriada={() => {
            setMostrarFormulario(false);
            carregar(filtroStatus);
          }}
          onCancelar={() => setMostrarFormulario(false)}
        />
      )}

      {turmaParaLider && (
        <FormularioLiderTurma
          turma={turmaParaLider}
          onDefinido={() => {
            setTurmaParaLider(null);
            carregar(filtroStatus);
          }}
          onCancelar={() => setTurmaParaLider(null)}
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
                <th className="px-4 py-3">Campus</th>
                <th className="px-4 py-3">Status</th>
                <th className="px-4 py-3"></th>
              </tr>
            </thead>
            <tbody>
              {turmasFiltradas.map((turma) => (
                <tr key={turma.id} className="border-b border-neutral-100 last:border-0">
                  <td className="px-4 py-3 font-medium text-neutral-900">{turma.nome}</td>
                  <td className="px-4 py-3 text-neutral-600">{turma.cursoNome}</td>
                  <td className="px-4 py-3 text-neutral-600">{turma.campusNome ?? "—"}</td>
                  <td className="px-4 py-3">
                    <span
                      className={`rounded-full px-2 py-0.5 text-xs font-medium ${
                        turma.status === "ATIVA" ? "bg-emerald-100 text-emerald-800" : "bg-neutral-100 text-neutral-600"
                      }`}
                    >
                      {turma.status === "ATIVA" ? "Ativa" : "Encerrada"}
                    </span>
                  </td>
                  <td className="px-4 py-3">
                    <div className="flex items-center justify-end gap-1">
                      {podeDelegarLiderP3 && (
                        <button
                          type="button"
                          onClick={() => setTurmaParaLider(turma)}
                          aria-label={`Definir líder de ${turma.nome}`}
                          title="Definir líder"
                          className="rounded-lg p-1.5 text-neutral-500 transition-colors hover:bg-neutral-100 hover:text-neutral-900 focus:outline-none focus-visible:ring-2 focus-visible:ring-neutral-800"
                        >
                          <svg
                            xmlns="http://www.w3.org/2000/svg"
                            viewBox="0 0 24 24"
                            fill="none"
                            stroke="currentColor"
                            strokeWidth={1.8}
                            className="size-5"
                          >
                            <path
                              strokeLinecap="round"
                              strokeLinejoin="round"
                              d="M18 7.5v3m0 0v3m0-3h3m-3 0h-3m-2.25-4.125a3.375 3.375 0 1 1-6.75 0 3.375 3.375 0 0 1 6.75 0ZM3 19.235v-.11a6.375 6.375 0 0 1 12.75 0v.109A12.318 12.318 0 0 1 9.374 21c-2.331 0-4.512-.645-6.374-1.766Z"
                            />
                          </svg>
                        </button>
                      )}
                      <Link
                        href={`/turmas/${turma.id}`}
                        aria-label={`Gerenciar ${turma.nome}`}
                        title="Gerenciar"
                        className="rounded-lg p-1.5 text-neutral-500 transition-colors hover:bg-neutral-100 hover:text-neutral-900 focus:outline-none focus-visible:ring-2 focus-visible:ring-neutral-800"
                      >
                        <svg
                          xmlns="http://www.w3.org/2000/svg"
                          viewBox="0 0 24 24"
                          fill="none"
                          stroke="currentColor"
                          strokeWidth={1.8}
                          className="size-5"
                        >
                          <path
                            strokeLinecap="round"
                            strokeLinejoin="round"
                            d="M16.862 4.487l1.687-1.688a1.875 1.875 0 1 1 2.652 2.652L10.582 16.07a4.5 4.5 0 0 1-1.897 1.13L6 18l.8-2.685a4.5 4.5 0 0 1 1.13-1.897l8.932-8.931Zm0 0L19.5 7.125"
                          />
                          <path
                            strokeLinecap="round"
                            strokeLinejoin="round"
                            d="M18 14v4.75A2.25 2.25 0 0 1 15.75 21H5.25A2.25 2.25 0 0 1 3 18.75V8.25A2.25 2.25 0 0 1 5.25 6H10"
                          />
                        </svg>
                      </Link>
                    </div>
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
