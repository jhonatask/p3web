import type { TurmaPublica } from "@/lib/api";

type CabecalhoTurmaProps = {
  turma: TurmaPublica;
};

export default function CabecalhoTurma({ turma }: CabecalhoTurmaProps) {
  return (
    <div className="mb-6 text-center">
      <p className="text-sm font-medium uppercase tracking-wide text-neutral-500">{turma.cursoNome}</p>
      <h1 className="mt-1 text-xl font-semibold text-neutral-900">{turma.turmaNome}</h1>
      <p className="mt-2 text-sm text-neutral-600">
        Confirme se esta é a sua turma e preencha seus dados para concluir o cadastro.
      </p>
    </div>
  );
}
