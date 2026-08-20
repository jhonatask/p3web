"use client";

import { useEffect, useState } from "react";
import { useParams } from "next/navigation";
import Link from "next/link";
import { obterPainel, gerarLinkWhatsapp, ErroApi, type PainelTurma, type PainelLinha, type ModuloAula } from "@/lib/api";

const ROTULOS_MODULO: Record<ModuloAula, string> = {
  INTRODUTORIO: "Introdutório",
  PLATAFORMA: "Plataforma",
  PROTECAO: "Proteção",
  PROVISAO: "Provisão",
};

const MODULOS_FORMATURA: ModuloAula[] = ["PLATAFORMA", "PROTECAO", "PROVISAO"];

const ROTULOS_STATUS_GERAL: Record<string, string> = {
  APTO_COMPLETO: "Apto · completo",
  APTO_REPOR_RECOMENDADO: "Apto · repor recomendado",
  NAO_FORMA: "Não forma · repor obrigatório",
};

function textoModulo(linha: PainelLinha, modulo: ModuloAula): string {
  const statusModulo = linha.formatura.modulos.find((item) => item.modulo === modulo);
  if (!statusModulo) {
    return "—";
  }
  if (statusModulo.faltas.length === 0) {
    return "completo";
  }
  const numeros = statusModulo.faltas.map((falta) => String(falta.numero).padStart(2, "0")).join(", ");
  return `falta ${statusModulo.faltas.length === 1 ? "Aula " : ""}${numeros}`;
}

export default function PaginaPainelTurma() {
  const params = useParams<{ id: string }>();
  const turmaId = params.id;

  const [painel, setPainel] = useState<PainelTurma | null>(null);
  const [erro, setErro] = useState<string | null>(null);
  const [linkGerando, setLinkGerando] = useState<string | null>(null);

  async function carregar() {
    try {
      const painelCarregado = await obterPainel(turmaId);
      setPainel(painelCarregado);
    } catch {
      setErro("Não foi possível carregar o painel.");
    }
  }

  useEffect(() => {
    // eslint-disable-next-line react-hooks/set-state-in-effect -- busca inicial de dados no mount
    carregar();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [turmaId]);

  async function notificar(participanteId: string) {
    setErro(null);
    setLinkGerando(participanteId);
    try {
      const { link } = await gerarLinkWhatsapp(turmaId, participanteId);
      window.open(link, "_blank", "noopener,noreferrer");
    } catch (erroCapturado) {
      setErro(erroCapturado instanceof ErroApi ? erroCapturado.message : "Não foi possível gerar o link.");
    } finally {
      setLinkGerando(null);
    }
  }

  if (erro && !painel) {
    return (
      <p role="alert" className="text-sm text-red-700">
        {erro}
      </p>
    );
  }

  if (!painel) {
    return <p className="text-sm text-neutral-500">Carregando...</p>;
  }

  return (
    <div className="space-y-6">
      <div>
        <Link href={`/turmas/${turmaId}`} className="text-sm text-neutral-500 hover:underline">
          ← {painel.turmaNome ?? "Turma"}
        </Link>
        <h1 className="mt-2 text-xl font-semibold text-neutral-900">Formatura e reposição</h1>
      </div>

      {erro && (
        <div role="alert" className="rounded-lg border border-red-300 bg-red-50 px-3 py-2 text-sm text-red-700">
          {erro}
        </div>
      )}

      {painel.linhas.length === 0 ? (
        <p className="text-sm text-neutral-500">Nenhum aluno matriculado nesta turma ainda.</p>
      ) : (
        <div className="overflow-x-auto rounded-xl border border-neutral-200 bg-white">
          <table className="w-full text-left text-sm">
            <thead className="border-b border-neutral-200 bg-neutral-50 text-xs uppercase tracking-wide text-neutral-500">
              <tr>
                <th className="px-4 py-3">Aluno</th>
                {MODULOS_FORMATURA.map((modulo) => (
                  <th key={modulo} className="px-4 py-3">
                    {ROTULOS_MODULO[modulo]}
                  </th>
                ))}
                <th className="px-4 py-3">Repor</th>
                <th className="px-4 py-3">Status</th>
                <th className="px-4 py-3"></th>
              </tr>
            </thead>
            <tbody>
              {painel.linhas.map((linha) => (
                <tr key={linha.participanteId} className="border-b border-neutral-100 last:border-0">
                  <td className="px-4 py-3 font-medium text-neutral-900">{linha.nome}</td>
                  {MODULOS_FORMATURA.map((modulo) => (
                    <td key={modulo} className="px-4 py-3 text-neutral-600">
                      {textoModulo(linha, modulo)}
                    </td>
                  ))}
                  <td className="px-4 py-3 text-neutral-600">
                    {linha.formatura.aRepor.length === 0
                      ? "—"
                      : linha.formatura.aRepor.map((falta) => `Aula ${String(falta.numero).padStart(2, "0")}`).join(", ")}
                  </td>
                  <td className="px-4 py-3">
                    <span
                      className={`rounded-full px-2 py-0.5 text-xs font-medium ${
                        linha.formatura.statusGeral === "NAO_FORMA"
                          ? "bg-red-100 text-red-800"
                          : linha.formatura.statusGeral === "APTO_REPOR_RECOMENDADO"
                            ? "bg-amber-100 text-amber-800"
                            : "bg-emerald-100 text-emerald-800"
                      }`}
                    >
                      {ROTULOS_STATUS_GERAL[linha.formatura.statusGeral] ?? linha.formatura.statusGeral}
                    </span>
                  </td>
                  <td className="px-4 py-3 text-right">
                    {linha.formatura.aRepor.length > 0 && (
                      <button
                        type="button"
                        onClick={() => notificar(linha.participanteId)}
                        disabled={linkGerando === linha.participanteId}
                        className="text-sm font-medium text-neutral-900 hover:underline disabled:text-neutral-400"
                      >
                        {linkGerando === linha.participanteId ? "Gerando..." : "Notificar reposição"}
                      </button>
                    )}
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
