"use client";

import { useRouter } from "next/navigation";
import CartaoPagina from "@/components/CartaoPagina";
import { limparSessao } from "@/lib/auth";

export default function PaginaSemAcesso() {
  const router = useRouter();

  function sair() {
    limparSessao();
    router.push("/login");
  }

  return (
    <CartaoPagina>
      <div className="text-center">
        <h1 className="text-xl font-semibold text-neutral-900">Sem acesso</h1>
        <p className="mt-3 text-neutral-600">
          Sua conta está logada, mas não tem papel de liderança no P3. Se você acha que deveria ter
          acesso, fale com a liderança responsável pela sua turma.
        </p>
        <button
          type="button"
          onClick={sair}
          className="mt-6 rounded-lg border border-neutral-300 px-4 py-2 text-sm font-medium text-neutral-700 transition-colors hover:border-neutral-400 focus:outline-none focus-visible:ring-2 focus-visible:ring-neutral-800"
        >
          Sair
        </button>
      </div>
    </CartaoPagina>
  );
}
