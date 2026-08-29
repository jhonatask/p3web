import type { Metadata } from "next";
import { Suspense } from "react";
import AtualizarCadastroBaseCliente from "@/components/AtualizarCadastroBaseCliente";

export const metadata: Metadata = {
  title: "Atualizar Cadastro — Cadastro P3",
  description: "Atualize os dados do seu Cadastro Base do Homem.",
};

export default function PaginaAtualizarCadastroBase() {
  return (
    <Suspense>
      <AtualizarCadastroBaseCliente />
    </Suspense>
  );
}
