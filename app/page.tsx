import CartaoPagina from "@/components/CartaoPagina";

export default function Home() {
  return (
    <CartaoPagina>
      <div className="text-center">
        <h1 className="text-xl font-semibold text-neutral-900">Cadastro P3</h1>
        <p className="mt-3 text-neutral-600">
          Esta é a página de cadastro de alunos. Acesse pelo link enviado pela liderança da sua turma
          para concluir o seu cadastro.
        </p>
      </div>
    </CartaoPagina>
  );
}
