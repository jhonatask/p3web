import type { AulaAtiva, TurmaPublica } from "@/lib/api";

type CabecalhoChamadaProps = {
  turma: TurmaPublica;
  aulaAtiva: AulaAtiva;
};

const ROTULOS_MODULO: Record<string, string> = {
  INTRODUTORIO: "Aula introdutória",
  PLATAFORMA: "Módulo Plataforma",
  PROTECAO: "Módulo Proteção",
  PROVISAO: "Módulo Provisão",
};

export default function CabecalhoChamada({ turma, aulaAtiva }: CabecalhoChamadaProps) {
  const rotuloModulo = ROTULOS_MODULO[aulaAtiva.modulo] ?? aulaAtiva.modulo;
  const rotuloAula =
    aulaAtiva.numero > 0
      ? `${rotuloModulo} · Aula ${String(aulaAtiva.numero).padStart(2, "0")} · ${aulaAtiva.tema}`
      : `${rotuloModulo} · ${aulaAtiva.tema}`;

  return (
    <div className="mb-6 text-center">
      <p className="text-sm font-medium uppercase tracking-wide text-neutral-500">{turma.cursoNome}</p>
      <h1 className="mt-1 text-xl font-semibold text-neutral-900">{turma.turmaNome}</h1>
      <p className="mt-2 text-sm text-neutral-600">Chamada da aula: {rotuloAula}</p>
      <p className="mt-3 text-sm text-neutral-500">Informe seu CPF para continuar.</p>
    </div>
  );
}
