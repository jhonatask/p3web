import CartaoPagina from "./CartaoPagina";

export default function SemAulaAtiva() {
  return (
    <CartaoPagina>
      <div className="text-center">
        <h1 className="text-xl font-semibold text-neutral-900">Nenhuma aula hoje</h1>
        <p className="mt-3 text-neutral-600">
          Essa turma não tem aula ativa no momento. Se você faltou a alguma aula, a reposição é feita
          acompanhando a mesma aula ao vivo em outra turma — fale com a liderança para saber onde.
        </p>
      </div>
    </CartaoPagina>
  );
}
