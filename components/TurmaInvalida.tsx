import CartaoPagina from "./CartaoPagina";

export default function TurmaInvalida() {
  return (
    <CartaoPagina>
      <div className="text-center">
        <h1 className="text-xl font-semibold text-neutral-900">Link inválido</h1>
        <p className="mt-3 text-neutral-600">
          Não encontramos nenhuma turma associada a este link de cadastro. Confira se o endereço foi
          copiado corretamente ou peça um novo link à liderança da sua turma.
        </p>
      </div>
    </CartaoPagina>
  );
}
