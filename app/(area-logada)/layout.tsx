"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import Image from "next/image";
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
          <Link href="/inicio" className="flex items-center gap-2 text-sm font-semibold text-neutral-900">
            <Image src="/logo-p3.png" alt="" width={28} height={28} />
            P3 · Área do líder
          </Link>
          <div className="flex items-center gap-4 text-sm text-neutral-600">
            <span>Olá, {nome}</span>
            <Link
              href="/configuracoes"
              aria-label="Configurações"
              title="Configurações"
              className="rounded-lg p-1.5 text-neutral-500 transition-colors hover:bg-neutral-100 hover:text-neutral-900 focus:outline-none focus-visible:ring-2 focus-visible:ring-neutral-800"
            >
              <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth={1.8} className="size-5">
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  d="M10.343 3.94c.09-.542.56-.94 1.11-.94h1.093c.55 0 1.02.398 1.11.94l.149.894c.07.424.384.764.78.93.398.164.855.142 1.205-.108l.737-.527a1.125 1.125 0 0 1 1.45.12l.773.774c.39.389.44 1.002.12 1.45l-.527.737c-.25.35-.272.806-.107 1.204.165.397.505.71.93.78l.893.15c.543.09.94.559.94 1.109v1.094c0 .55-.397 1.02-.94 1.11l-.893.149c-.425.07-.765.383-.93.78-.165.398-.143.854.107 1.204l.527.738c.32.447.269 1.06-.12 1.45l-.774.773a1.125 1.125 0 0 1-1.449.12l-.738-.527c-.35-.25-.806-.272-1.203-.107-.397.165-.71.505-.781.929l-.149.894c-.09.542-.56.94-1.11.94h-1.094c-.55 0-1.019-.398-1.11-.94l-.148-.894c-.071-.424-.384-.764-.781-.93-.398-.164-.854-.142-1.204.108l-.738.527c-.447.32-1.06.269-1.45-.12l-.773-.774a1.125 1.125 0 0 1-.12-1.45l.527-.737c.25-.35.273-.806.108-1.204-.165-.397-.505-.71-.93-.78l-.894-.15c-.542-.09-.94-.56-.94-1.109v-1.094c0-.55.398-1.02.94-1.11l.894-.149c.424-.07.765-.383.93-.78.165-.398.143-.854-.108-1.204l-.526-.738a1.125 1.125 0 0 1 .12-1.45l.773-.773a1.125 1.125 0 0 1 1.45-.12l.737.527c.35.25.807.272 1.204.107.397-.165.71-.505.78-.929l.15-.894Z"
                />
                <path strokeLinecap="round" strokeLinejoin="round" d="M15 12a3 3 0 1 1-6 0 3 3 0 0 1 6 0Z" />
              </svg>
            </Link>
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
