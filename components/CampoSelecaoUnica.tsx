export type OpcaoSelecao<T extends string> = {
  valor: T;
  rotulo: string;
};

type CampoSelecaoUnicaProps<T extends string> = {
  label: string;
  opcoes: OpcaoSelecao<T>[];
  valor: T | null;
  onChange: (valor: T) => void;
  erro?: string;
};

export default function CampoSelecaoUnica<T extends string>({
  label,
  opcoes,
  valor,
  onChange,
  erro,
}: CampoSelecaoUnicaProps<T>) {
  return (
    <div>
      <span className="block text-sm font-medium text-neutral-800">{label}</span>
      <div className="mt-1 flex flex-wrap gap-2" role="radiogroup" aria-label={label}>
        {opcoes.map((opcao) => {
          const selecionada = valor === opcao.valor;
          return (
            <button
              key={opcao.valor}
              type="button"
              role="radio"
              aria-checked={selecionada}
              onClick={() => onChange(opcao.valor)}
              className={`rounded-lg border px-3 py-1.5 text-sm transition-colors focus:outline-none focus-visible:ring-2 focus-visible:ring-neutral-800 ${
                selecionada
                  ? "border-neutral-800 bg-neutral-900 text-white"
                  : "border-neutral-300 text-neutral-700 hover:border-neutral-400"
              }`}
            >
              {opcao.rotulo}
            </button>
          );
        })}
      </div>
      {erro && (
        <p role="alert" className="mt-1 text-sm text-red-600">
          {erro}
        </p>
      )}
    </div>
  );
}
