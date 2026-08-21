import CartaoPagina from "@/components/CartaoPagina";

export default function Home() {
  return (
    <CartaoPagina>
      <div className="text-center">
        <h1 className="text-xl font-semibold text-neutral-900">P3</h1>
        <p className="mt-3 text-neutral-600">
          Cadastro e chamada da Plataforma P3. Acesse pelo link enviado pela liderança da sua turma para
          se cadastrar ou marcar presença.
        </p>
      </div>
    </CartaoPagina>
  );
}
