type CampoEscalaProps = {
  label: string;
  valor: number | null;
  onChange: (valor: number) => void;
  erro?: string;
};

const NOTAS = Array.from({ length: 11 }, (_, indice) => indice);

export default function CampoEscala({ label, valor, onChange, erro }: CampoEscalaProps) {
  return (
    <div>
      <span className="block text-sm font-medium text-neutral-800">{label}</span>
      <div className="mt-1 flex flex-wrap gap-1.5" role="radiogroup" aria-label={label}>
        {NOTAS.map((nota) => {
          const selecionada = valor === nota;
          return (
            <button
              key={nota}
              type="button"
              role="radio"
              aria-checked={selecionada}
              onClick={() => onChange(nota)}
              className={`flex h-9 w-9 items-center justify-center rounded-lg border text-sm transition-colors focus:outline-none focus-visible:ring-2 focus-visible:ring-neutral-800 ${
                selecionada
                  ? "border-neutral-800 bg-neutral-900 text-white"
                  : "border-neutral-300 text-neutral-700 hover:border-neutral-400"
              }`}
            >
              {nota}
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
