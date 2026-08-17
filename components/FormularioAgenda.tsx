"use client";

import { useEffect, useState } from "react";
import { definirAgenda, ErroApi, type AulaAdmin, type TipoAulaResponse } from "@/lib/api";

type FormularioAgendaProps = {
  turmaId: string;
  tiposAula: TipoAulaResponse[];
  aulas: AulaAdmin[];
  onAtualizado: (aulas: AulaAdmin[]) => void;
};

const ROTULOS_MODULO: Record<string, string> = {
  INTRODUTORIO: "Introdutório",
  PLATAFORMA: "Plataforma",
  PROTECAO: "Proteção",
  PROVISAO: "Provisão",
};

const ROTULOS_STATUS: Record<string, string> = {
  AGENDADA: "Agendada",
  ATIVA: "Ativa",
  ENCERRADA: "Encerrada",
};

export default function FormularioAgenda({ turmaId, tiposAula, aulas, onAtualizado }: FormularioAgendaProps) {
  const [datas, setDatas] = useState<Record<string, string>>({});
  const [erro, setErro] = useState<string | null>(null);
  const [salvando, setSalvando] = useState(false);

  // `aulas` chega de forma assincrona (a turma carrega os dados depois do
  // primeiro render) - um useState inicial nao pegaria essa atualizacao, por
  // isso resincroniza sempre que a lista de aulas mudar (carregamento inicial
  // ou apos salvar).
  useEffect(() => {
    const novasDatas: Record<string, string> = {};
    for (const aula of aulas) {
      novasDatas[aula.tipoAulaId] = aula.data;
    }
    // eslint-disable-next-line react-hooks/set-state-in-effect -- sincroniza com a prop assincrona
    setDatas(novasDatas);
  }, [aulas]);

  function aulaDe(tipoAulaId: string): AulaAdmin | null {
    return aulas.find((aula) => aula.tipoAulaId === tipoAulaId) ?? null;
  }

  async function salvar() {
    setErro(null);
    const itens = Object.entries(datas)
      .filter(([, data]) => data)
      .map(([tipoAulaId, data]) => ({ tipoAulaId, data }));

    if (itens.length === 0) {
      setErro("Defina ao menos uma data.");
      return;
    }

    setSalvando(true);
    try {
      const aulasAtualizadas = await definirAgenda(turmaId, itens);
      onAtualizado(aulasAtualizadas);
    } catch (erroCapturado) {
      setErro(erroCapturado instanceof ErroApi ? erroCapturado.message : "Não foi possível salvar a agenda.");
    } finally {
      setSalvando(false);
    }
  }

  return (
    <div className="space-y-4 rounded-xl border border-neutral-200 bg-white p-5">
      {erro && (
        <div role="alert" className="rounded-lg border border-red-300 bg-red-50 px-3 py-2 text-sm text-red-700">
          {erro}
        </div>
      )}

      {tiposAula.length === 0 && (
        <p className="text-sm text-neutral-500">
          Este curso ainda não tem tipos de aula cadastrados no catálogo — confirme se a turma está
          vinculada ao curso certo.
        </p>
      )}

      <div className="space-y-3">
        {tiposAula.map((tipo) => {
          const aula = aulaDe(tipo.id);
          const status = aula?.status ?? null;
          const bloqueada = status === "ENCERRADA" && !aula?.encerradaManualmente;
          return (
            <div key={tipo.id} className="flex flex-wrap items-center justify-between gap-3">
              <div>
                <p className="text-sm font-medium text-neutral-900">
                  {tipo.numero === 0 ? "Introdutória" : `Aula ${String(tipo.numero).padStart(2, "0")}`} · {tipo.tema}
                </p>
                <p className="text-xs text-neutral-500">{ROTULOS_MODULO[tipo.modulo] ?? tipo.modulo}</p>
              </div>
              <div className="flex items-center gap-2">
                {status && (
                  <span
                    className={`rounded-full px-2 py-0.5 text-xs font-medium ${
                      status === "ATIVA"
                        ? "bg-emerald-100 text-emerald-800"
                        : status === "ENCERRADA"
                          ? "bg-neutral-200 text-neutral-600"
                          : "bg-amber-100 text-amber-800"
                    }`}
                  >
                    {ROTULOS_STATUS[status] ?? status}
                  </span>
                )}
                <input
                  type="date"
                  value={datas[tipo.id] ?? ""}
                  disabled={bloqueada}
                  onChange={(evento) => setDatas((atual) => ({ ...atual, [tipo.id]: evento.target.value }))}
                  className="rounded-lg border border-neutral-300 px-3 py-1.5 text-sm text-neutral-900 focus:outline-none focus-visible:ring-2 focus-visible:ring-neutral-800 disabled:bg-neutral-100 disabled:text-neutral-400"
                />
              </div>
            </div>
          );
        })}
      </div>

      {tiposAula.length > 0 && (
        <button
          type="button"
          onClick={salvar}
          disabled={salvando}
          className="rounded-lg bg-neutral-900 px-4 py-2 text-sm font-medium text-white transition-colors focus:outline-none focus-visible:ring-2 focus-visible:ring-neutral-800 disabled:cursor-not-allowed disabled:bg-neutral-300"
        >
          {salvando ? "Salvando..." : "Salvar agenda"}
        </button>
      )}
    </div>
  );
}
