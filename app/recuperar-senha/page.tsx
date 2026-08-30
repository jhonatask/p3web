import type { Metadata } from "next";
import RecuperarSenhaCliente from "@/components/RecuperarSenhaCliente";

export const metadata: Metadata = {
  title: "Recuperar senha — P3",
  description: "Recuperação de senha da área do líder.",
};

export default function PaginaRecuperarSenha() {
  return <RecuperarSenhaCliente />;
}
