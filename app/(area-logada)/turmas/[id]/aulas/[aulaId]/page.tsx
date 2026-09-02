"use client";

import { useEffect, useState } from "react";
import { useParams } from "next/navigation";
import Link from "next/link";
import {
  buscarAulaPorId,
  listarAlunosDaTurma,
  listarPresencasDaAula,
  marcarPresencaManual,
  ErroApi,
  type AulaAdmin,
  type AlunoAdmin,
  type PresencaAdmin,
} from "@/lib/api";
import { mascararCpfParcial } from "@/lib/masks";
import { paginar, totalDePaginas } from "@/lib/paginacao";
import Paginador from "@/components/Paginador";
import { IconeCheckCirculo, IconeUsuarios } from "@/components/Icones";

export default function PaginaPresencaAula() {
  const params = useParams<{ id: string; aulaId: string }>();
  const turmaId = params.id;
  const aulaId = params.aulaId;

  const [aula, setAula] = useState<AulaAdmin | null>(null);
  const [alunos, setAlunos] = useState<AlunoAdmin[]>([]);
  const [presencas, setPresencas] = useState<PresencaAdmin[]>([]);
  const [erro, setErro] = useState<string | null>(null);
  const [marcando, setMarcando] = useState<string | null>(null);
  const [pagina, setPagina] = useState(1);

  async function carregar() {
    try {
      const aulaCarregada = await buscarAulaPorId(aulaId);
      setAula(aulaCarregada);
      const [alunosCarregados, presencasCarregadas] = await Promise.all([
        listarAlunosDaTurma(aulaCarregada.turmaId),
        listarPresencasDaAula(aulaId),
      ]);
      setAlunos(alunosCarregados);
      setPresencas(presencasCarregadas);
    } catch {
      setErro("Não foi possível carregar a aula.");
    }
  }

  useEffect(() => {
    // eslint-disable-next-line react-hooks/set-state-in-effect -- busca inicial de dados no mount
    carregar();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [aulaId]);

  async function aoMarcarPresenca(cpf: string) {
    setErro(null);
    setMarcando(cpf);
    try {
      const presenca = await marcarPresencaManual(aulaId, cpf);
      setPresencas((atual) => [...atual, presenca]);
    } catch (erroCapturado) {
      setErro(erroCapturado instanceof ErroApi ? erroCapturado.message : "Não foi possível marcar a presença.");
    } finally {
      setMarcando(null);
    }
  }

  if (erro && !aula) {
    return (
      <p role="alert" className="text-sm text-red-700">
        {erro}
      </p>
    );
  }

  if (!aula) {
    return <p className="text-sm text-neutral-500">Carregando...</p>;
  }

  const presencaPorCpf = new Map(alunos.map((aluno) => [aluno.cpf, presencas.find((p) => p.participanteId === aluno.participanteId)]));

  return (
    <div className="space-y-6">
      <div>
        <Link href={`/turmas/${turmaId}`} className="text-sm text-neutral-500 hover:underline">
          ← Turma
        </Link>
        <h1 className="mt-2 text-xl font-semibold text-neutral-900">
          {aula.numero === 0 ? "Introdutória" : `Aula ${String(aula.numero).padStart(2, "0")}`} · {aula.tema}
        </h1>
        <p className="text-sm text-neutral-600">
          {new Date(`${aula.data}T00:00:00`).toLocaleDateString("pt-BR")} — presença manual pra alunos que não fizeram
          a chamada pelo link.
        </p>
        <p className="mt-2 flex items-center gap-1.5 text-sm font-semibold uppercase tracking-wide text-neutral-500">
          <IconeUsuarios className="h-4 w-4 text-neutral-400" />
          Alunos ({alunos.length})
        </p>
      </div>

      {erro && (
        <div role="alert" className="rounded-lg border border-red-300 bg-red-50 px-3 py-2 text-sm text-red-700">
          {erro}
        </div>
      )}

      {alunos.length === 0 ? (
        <p className="text-sm text-neutral-500">Ninguém matriculado nesta turma ainda.</p>
      ) : (
        <div className="overflow-hidden rounded-xl border border-neutral-200 bg-white">
          <table className="w-full text-left text-sm">
            <thead className="border-b border-neutral-200 bg-neutral-50 text-xs uppercase tracking-wide text-neutral-500">
              <tr>
                <th className="px-4 py-3">Nome</th>
                <th className="px-4 py-3">CPF</th>
                <th className="px-4 py-3">Presença</th>
                <th className="px-4 py-3"></th>
              </tr>
            </thead>
            <tbody>
              {paginar(alunos, pagina).map((aluno) => {
                const presenca = presencaPorCpf.get(aluno.cpf);
                return (
                  <tr key={aluno.id} className="border-b border-neutral-100 last:border-0">
                    <td className="px-4 py-3 font-medium text-neutral-900">{aluno.nome}</td>
                    <td className="px-4 py-3 text-neutral-600">{mascararCpfParcial(aluno.cpf)}</td>
                    <td className="px-4 py-3">
                      {presenca ? (
                        <span className="rounded-full bg-emerald-100 px-2 py-0.5 text-xs font-medium text-emerald-800">
                          Presente{presenca.metodo === "MANUAL" ? " · manual" : ""}
                        </span>
                      ) : (
                        <span className="rounded-full bg-neutral-100 px-2 py-0.5 text-xs font-medium text-neutral-600">
                          Sem registro
                        </span>
                      )}
                    </td>
                    <td className="px-4 py-3 text-right">
                      {!presenca && (
                        <button
                          type="button"
                          onClick={() => aoMarcarPresenca(aluno.cpf)}
                          disabled={marcando === aluno.cpf}
                          title="Marcar presença"
                          aria-label={`Marcar presença de ${aluno.nome}`}
                          className="inline-flex rounded p-1.5 text-neutral-700 transition-colors hover:bg-neutral-100 disabled:text-neutral-300"
                        >
                          <IconeCheckCirculo className={marcando === aluno.cpf ? "h-4 w-4 animate-pulse" : "h-4 w-4"} />
                        </button>
                      )}
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
          <Paginador pagina={pagina} totalPaginas={totalDePaginas(alunos.length)} onMudarPagina={setPagina} />
        </div>
      )}
    </div>
  );
}
