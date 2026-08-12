import type { Metadata } from "next";
import CartaoPagina from "@/components/CartaoPagina";
import FormularioLogin from "@/components/FormularioLogin";

export const metadata: Metadata = {
  title: "Login — P3",
  description: "Acesso da área do líder.",
};

export default function PaginaLogin() {
  return (
    <CartaoPagina>
      <div className="mb-6 text-center">
        <h1 className="text-xl font-semibold text-neutral-900">Área do líder</h1>
        <p className="mt-2 text-sm text-neutral-600">Entre com seu e-mail e senha.</p>
      </div>
      <FormularioLogin />
    </CartaoPagina>
  );
}
