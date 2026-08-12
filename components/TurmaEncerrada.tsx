import CartaoPagina from "./CartaoPagina";

type TurmaEncerradaProps = {
  mensagem: string;
};

export default function TurmaEncerrada({ mensagem }: TurmaEncerradaProps) {
  return (
    <CartaoPagina>
      <div className="text-center">
        <h1 className="text-xl font-semibold text-neutral-900">Turma encerrada</h1>
        <p className="mt-3 text-neutral-600">{mensagem}</p>
      </div>
    </CartaoPagina>
  );
}
