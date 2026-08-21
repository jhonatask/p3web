import CartaoPagina from "./CartaoPagina";

export default function ErroCarregamento() {
  return (
    <CartaoPagina>
      <div className="text-center">
        <h1 className="text-xl font-semibold text-neutral-900">Não foi possível carregar</h1>
        <p className="mt-3 text-neutral-600">
          Tivemos um problema ao buscar as informações da sua turma. Verifique sua conexão e tente
          novamente em instantes.
        </p>
      </div>
    </CartaoPagina>
  );
}
