import Link from "next/link";

/**
 * Um card por modulo da Plataforma P3. Hoje so a Escola de Hombridade esta
 * implementada - novos modulos (Homem ao Maximo, Primeiros Passos, Servos,
 * GCs de Homens) entram aqui conforme forem construidos. Ainda nao ha
 * controle de acesso por modulo no backend (Papel e generico, nao por
 * modulo) - todo mundo que loga ve todos os cards por enquanto.
 */
const MODULOS = [
  {
    titulo: "Escola de Hombridade",
    descricao: "Turmas, agenda de aulas, QR code e o painel de formatura dos alunos.",
    href: "/turmas",
  },
];

export default function PaginaInicio() {
  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-xl font-semibold text-neutral-900">Área do líder</h1>
        <p className="mt-1 text-sm text-neutral-600">Escolha o módulo que você gerencia.</p>
      </div>

      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
        {MODULOS.map((modulo) => (
          <Link
            key={modulo.href}
            href={modulo.href}
            className="rounded-xl border border-neutral-200 bg-white p-5 transition-colors hover:border-neutral-400 focus:outline-none focus-visible:ring-2 focus-visible:ring-neutral-800"
          >
            <h2 className="text-base font-semibold text-neutral-900">{modulo.titulo}</h2>
            <p className="mt-2 text-sm text-neutral-600">{modulo.descricao}</p>
          </Link>
        ))}
      </div>
    </div>
  );
}
