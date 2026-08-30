"use client";

import { useEffect, useState } from "react";
import { useParams } from "next/navigation";
import Link from "next/link";
import {
  buscarTurmaAdmin,
  listarTiposAulaDoCurso,
  listarAulasDaTurma,
  listarAlunosDaTurma,
  removerAlunoDaTurma,
  encerrarTurma,
  encerrarAulaAtual,
  desativarAulaAtual,
  baixarQrCodeTurma,
  ErroApi,
  type TurmaAdmin,
  type TipoAulaResponse,
  type AulaAdmin,
  type AlunoAdmin,
} from "@/lib/api";
import { mascararCpfParcial } from "@/lib/masks";
import { paginar, totalDePaginas } from "@/lib/paginacao";
import FormularioAgenda from "@/components/FormularioAgenda";
import Paginador from "@/components/Paginador";

export default function PaginaGestaoTurma() {
  const params = useParams<{ id: string }>();
  const turmaId = params.id;

  const [turma, setTurma] = useState<TurmaAdmin | null>(null);
  const [tiposAula, setTiposAula] = useState<TipoAulaResponse[]>([]);
  const [aulas, setAulas] = useState<AulaAdmin[]>([]);
  const [alunos, setAlunos] = useState<AlunoAdmin[]>([]);
  const [erro, setErro] = useState<string | null>(null);
  const [mensagem, setMensagem] = useState<string | null>(null);
  const [qrUrl, setQrUrl] = useState<string | null>(null);
  const [paginaAlunos, setPaginaAlunos] = useState(1);

  async function carregar() {
    try {
      const turmaCarregada = await buscarTurmaAdmin(turmaId);
      setTurma(turmaCarregada);
      const [tipos, aulasCarregadas, alunosCarregados] = await Promise.all([
        listarTiposAulaDoCurso(turmaCarregada.cursoId),
        listarAulasDaTurma(turmaId),
        listarAlunosDaTurma(turmaId),
      ]);
      setTiposAula(tipos);
      setAulas(aulasCarregadas);
      setAlunos(alunosCarregados);
    } catch {
      setErro("Não foi possível carregar a turma.");
    }
  }

  useEffect(() => {
    // eslint-disable-next-line react-hooks/set-state-in-effect -- busca inicial de dados no mount
    carregar();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [turmaId]);

  useEffect(() => {
    return () => {
      if (qrUrl) {
        URL.revokeObjectURL(qrUrl);
      }
    };
  }, [qrUrl]);

  async function mostrarQrCode() {
    setErro(null);
    try {
      const blob = await baixarQrCodeTurma(turmaId);
      const url = URL.createObjectURL(blob);
      setQrUrl(url);
    } catch {
      setErro("Não foi possível gerar o QR code.");
    }
  }

  async function aoEncerrarTurma() {
    if (!confirm("Encerrar esta turma? Ela para de aceitar chamada.")) {
      return;
    }
    try {
      const turmaAtualizada = await encerrarTurma(turmaId);
      setTurma(turmaAtualizada);
    } catch {
      setErro("Não foi possível encerrar a turma.");
    }
  }

  async function aoEncerrarAulaAtual() {
    setErro(null);
    setMensagem(null);
    try {
      const proxima = await encerrarAulaAtual(turmaId);
      setMensagem(
        proxima ? `Aula encerrada. Próxima aula ativa: ${proxima.tema}.` : "Aula encerrada. Não há mais aulas agendadas.",
      );
      const aulasAtualizadas = await listarAulasDaTurma(turmaId);
      setAulas(aulasAtualizadas);
    } catch (erroCapturado) {
      setErro(erroCapturado instanceof ErroApi ? erroCapturado.message : "Não foi possível avançar a aula.");
    }
  }

  async function aoDesativarAulaAtual() {
    setErro(null);
    setMensagem(null);
    try {
      await desativarAulaAtual(turmaId);
      setMensagem("Aula desativada. Nenhuma aula ficou ativa - ninguém consegue fazer a chamada até você abrir outra.");
      const aulasAtualizadas = await listarAulasDaTurma(turmaId);
      setAulas(aulasAtualizadas);
    } catch (erroCapturado) {
      setErro(erroCapturado instanceof ErroApi ? erroCapturado.message : "Não foi possível desativar a aula.");
    }
  }

  async function aoRemoverAluno(aluno: AlunoAdmin) {
    if (!confirm(`Remover ${aluno.nome} desta turma? Use pra matrícula feita na turma errada.`)) {
      return;
    }
    setErro(null);
    try {
      await removerAlunoDaTurma(turmaId, aluno.id);
      setAlunos((atual) => atual.filter((item) => item.id !== aluno.id));
    } catch (erroCapturado) {
      setErro(erroCapturado instanceof ErroApi ? erroCapturado.message : "Não foi possível remover o aluno.");
    }
  }

  if (erro && !turma) {
    return (
      <p role="alert" className="text-sm text-red-700">
        {erro}
      </p>
    );
  }

  if (!turma) {
    return <p className="text-sm text-neutral-500">Carregando...</p>;
  }

  const aulaAtivaAtual = aulas.find((aula) => aula.status === "ATIVA") ?? null;

  return (
    <div className="space-y-6">
      <div>
        <Link href="/turmas" className="text-sm text-neutral-500 hover:underline">
          ← Turmas
        </Link>
        <div className="mt-2 flex flex-wrap items-center justify-between gap-3">
          <div>
            <h1 className="text-xl font-semibold text-neutral-900">{turma.nome}</h1>
            <p className="text-sm text-neutral-600">{turma.cursoNome}</p>
          </div>
          <div className="flex gap-2">
            <Link
              href={`/turmas/${turmaId}/painel`}
              className="rounded-lg border border-neutral-300 px-4 py-2 text-sm font-medium text-neutral-700 transition-colors hover:border-neutral-400"
            >
              Formatura e reposição
            </Link>
            {turma.status === "ATIVA" && (
              <button
                type="button"
                onClick={aoEncerrarTurma}
                className="rounded-lg border border-red-300 px-4 py-2 text-sm font-medium text-red-700 transition-colors hover:border-red-400"
              >
                Encerrar turma
              </button>
            )}
          </div>
        </div>
      </div>

      {erro && (
        <div role="alert" className="rounded-lg border border-red-300 bg-red-50 px-3 py-2 text-sm text-red-700">
          {erro}
        </div>
      )}
      {mensagem && (
        <div role="status" className="rounded-lg border border-emerald-300 bg-emerald-50 px-3 py-2 text-sm text-emerald-800">
          {mensagem}
        </div>
      )}

      <div className="rounded-xl border border-neutral-200 bg-white p-5">
        <h2 className="text-sm font-semibold uppercase tracking-wide text-neutral-500">Link da chamada</h2>
        <p className="mt-2 break-all text-sm text-neutral-900">{turma.linkCadastro}</p>

        {aulaAtivaAtual ? (
          <p className="mt-3 inline-flex items-center gap-2 rounded-lg bg-emerald-50 px-3 py-2 text-sm text-emerald-800">
            <span className="h-2 w-2 shrink-0 rounded-full bg-emerald-500" />
            Aula ativa agora: {aulaAtivaAtual.numero === 0 ? "Introdutória" : `Aula ${String(aulaAtivaAtual.numero).padStart(2, "0")}`}{" "}
            · {aulaAtivaAtual.tema}
          </p>
        ) : (
          <p className="mt-3 inline-flex items-center gap-2 rounded-lg bg-neutral-100 px-3 py-2 text-sm text-neutral-600">
            <span className="h-2 w-2 shrink-0 rounded-full bg-neutral-400" />
            Nenhuma aula ativa no momento — quem acessar o link agora não consegue fazer a chamada.
          </p>
        )}

        <div className="mt-3 flex flex-wrap gap-3">
          <button
            type="button"
            onClick={mostrarQrCode}
            className="rounded-lg border border-neutral-300 px-4 py-2 text-sm text-neutral-700 transition-colors hover:border-neutral-400"
          >
            Gerar QR code
          </button>
          <button
            type="button"
            onClick={aoEncerrarAulaAtual}
            className="rounded-lg border border-neutral-300 px-4 py-2 text-sm text-neutral-700 transition-colors hover:border-neutral-400"
          >
            Encerrar aula atual manualmente
          </button>
          <button
            type="button"
            onClick={aoDesativarAulaAtual}
            className="rounded-lg border border-neutral-300 px-4 py-2 text-sm text-neutral-700 transition-colors hover:border-neutral-400"
          >
            Desativar aula atual
          </button>
        </div>
        {qrUrl && (
          <div className="mt-4">
            {/* eslint-disable-next-line @next/next/no-img-element -- blob: URL, next/image nao suporta */}
            <img src={qrUrl} alt="QR code da turma" className="h-48 w-48" />
            <a href={qrUrl} download={`qrcode-${turma.nome}.png`} className="mt-2 block text-sm text-neutral-600 hover:underline">
              Baixar imagem
            </a>
          </div>
        )}
      </div>

      <div>
        <h2 className="mb-3 text-sm font-semibold uppercase tracking-wide text-neutral-500">Agenda das aulas</h2>
        <FormularioAgenda turmaId={turmaId} tiposAula={tiposAula} aulas={aulas} onAtualizado={setAulas} />
      </div>

      <div>
        <h2 className="mb-3 text-sm font-semibold uppercase tracking-wide text-neutral-500">
          Alunos matriculados ({alunos.length})
        </h2>
        {alunos.length === 0 ? (
          <p className="text-sm text-neutral-500">
            Ninguém matriculado ainda — a matrícula acontece automaticamente na primeira chamada.
          </p>
        ) : (
          <div className="overflow-hidden rounded-xl border border-neutral-200 bg-white">
            <table className="w-full text-left text-sm">
              <thead className="border-b border-neutral-200 bg-neutral-50 text-xs uppercase tracking-wide text-neutral-500">
                <tr>
                  <th className="px-4 py-3">Nome</th>
                  <th className="px-4 py-3">CPF</th>
                  <th className="px-4 py-3"></th>
                </tr>
              </thead>
              <tbody>
                {paginar(alunos, paginaAlunos).map((aluno) => (
                  <tr key={aluno.id} className="border-b border-neutral-100 last:border-0">
                    <td className="px-4 py-3 font-medium text-neutral-900">{aluno.nome}</td>
                    <td className="px-4 py-3 text-neutral-600">{mascararCpfParcial(aluno.cpf)}</td>
                    <td className="px-4 py-3 text-right">
                      <button
                        type="button"
                        onClick={() => aoRemoverAluno(aluno)}
                        className="text-sm font-medium text-red-700 hover:underline"
                      >
                        Remover
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
            <Paginador
              pagina={paginaAlunos}
              totalPaginas={totalDePaginas(alunos.length)}
              onMudarPagina={setPaginaAlunos}
            />
          </div>
        )}
      </div>
    </div>
  );
}
