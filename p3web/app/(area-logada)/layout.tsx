"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import { estaAutenticado, limparSessao, obterNomeLogado } from "@/lib/auth";

export default function LayoutAreaLogada({ children }: { children: React.ReactNode }) {
  const router = useRouter();
  // Comeca igual no servidor e no cliente (false/null) de proposito: o
  // servidor nunca tem acesso ao localStorage, entao ler estaAutenticado()
  // direto no useState divergiria entre SSR e o primeiro render do cliente
  // (erro de hidratacao). A checagem real so acontece no efeito abaixo,
  // que roda exclusivamente no cliente, depois da hidratacao.
  const [pronto, setPronto] = useState(false);
  const [nome, setNome] = useState<string | null>(null);

  useEffect(() => {
    if (!estaAutenticado()) {
      router.replace("/login");
      return;
    }
    // eslint-disable-next-line react-hooks/set-state-in-effect -- sincroniza com localStorage, so existe no cliente
    setNome(obterNomeLogado());
    setPronto(true);
  }, [router]);

  function sair() {
    limparSessao();
    router.push("/login");
  }

  if (!pronto) {
    return null;
  }

  return (
    <div className="min-h-screen bg-neutral-50">
      <header className="border-b border-neutral-200 bg-white px-4 py-3">
        <div className="mx-auto flex max-w-5xl items-center justify-between">
          <Link href="/inicio" className="text-sm font-semibold text-neutral-900">
            P3 · Área do líder
          </Link>
          <div className="flex items-center gap-4 text-sm text-neutral-600">
            <span>Olá, {nome}</span>
            <button
              type="button"
              onClick={sair}
              className="rounded-lg border border-neutral-300 px-3 py-1.5 text-neutral-700 transition-colors hover:border-neutral-400 focus:outline-none focus-visible:ring-2 focus-visible:ring-neutral-800"
            >
              Sair
            </button>
          </div>
        </div>
      </header>
      <main className="mx-auto max-w-5xl px-4 py-8">{children}</main>
    </div>
  );
}
