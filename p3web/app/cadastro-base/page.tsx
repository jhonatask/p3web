import type { Metadata } from "next";
import CadastroBaseCliente from "@/components/CadastroBaseCliente";

export const metadata: Metadata = {
  title: "Cadastro Base do Homem — Cadastro P3",
  description: "Cadastro único por CPF, reaproveitado por todos os ministérios da Family.",
};

export default function PaginaCadastroBase() {
  return <CadastroBaseCliente />;
}
