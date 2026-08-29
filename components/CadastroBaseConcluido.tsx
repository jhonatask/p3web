import Link from "next/link";

type CadastroBaseConcluidoProps = {
  nome: string;
  turmaMatriculada?: string;
};

export default function CadastroBaseConcluido({ nome, turmaMatriculada }: CadastroBaseConcluidoProps) {
  const primeiroNome = nome.trim().split(/\s+/)[0];

  return (
    <div className="text-center">
      <h1 className="text-xl font-semibold text-neutral-900">Cadastro confirmado, {primeiroNome}!</h1>
      <p className="mt-3 text-neutral-600">
        Seu cadastro foi salvo na base única da Family. A partir de agora, os cursos e ministérios em que
        você participar vão te reconhecer pelo CPF — não é preciso preencher esses dados de novo.
      </p>
      {turmaMatriculada && (
        <p className="mt-3 rounded-lg bg-emerald-50 px-3 py-2 text-sm text-emerald-800">
          Matrícula confirmada na turma {turmaMatriculada}.
        </p>
      )}
      <p className="mt-4 text-sm text-neutral-500">Pode fechar esta página.</p>
      <p className="mt-2 text-sm">
        <Link href="/cadastro-base/atualizar" className="text-neutral-600 hover:underline">
          Precisa corrigir algo? Atualize seu cadastro.
        </Link>
      </p>
    </div>
  );
}
