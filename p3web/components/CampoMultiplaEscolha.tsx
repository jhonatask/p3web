type CampoMultiplaEscolhaProps = {
  label: string;
  descricao?: string;
  opcoes: string[];
  valores: string[];
  onChange: (valores: string[]) => void;
  erro?: string;
};

export default function CampoMultiplaEscolha({
  label,
  descricao,
  opcoes,
  valores,
  onChange,
  erro,
}: CampoMultiplaEscolhaProps) {
  function alternar(opcao: string) {
    if (valores.includes(opcao)) {
      onChange(valores.filter((valor) => valor !== opcao));
    } else {
      onChange([...valores, opcao]);
    }
  }

  return (
    <div>
      <span className="block text-sm font-medium text-neutral-800">
        {label}
        {descricao && <span className="ml-2 text-xs font-normal text-neutral-500">{descricao}</span>}
      </span>
      <div className="mt-1 flex flex-wrap gap-2">
        {opcoes.map((opcao) => {
          const selecionada = valores.includes(opcao);
          return (
            <button
              key={opcao}
              type="button"
              aria-pressed={selecionada}
              onClick={() => alternar(opcao)}
              className={`rounded-lg border px-3 py-1.5 text-sm transition-colors focus:outline-none focus-visible:ring-2 focus-visible:ring-neutral-800 ${
                selecionada
                  ? "border-amber-700 bg-amber-50 text-amber-900"
                  : "border-neutral-300 text-neutral-700 hover:border-neutral-400"
              }`}
            >
              {opcao}
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
