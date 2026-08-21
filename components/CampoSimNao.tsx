type CampoSimNaoProps = {
  label: string;
  valor: boolean | null;
  onChange: (valor: boolean) => void;
  erro?: string;
};

export default function CampoSimNao({ label, valor, onChange, erro }: CampoSimNaoProps) {
  return (
    <div>
      <span className="block text-sm font-medium text-neutral-800">{label}</span>
      <div className="mt-1 flex gap-2" role="radiogroup" aria-label={label}>
        <button
          type="button"
          role="radio"
          aria-checked={valor === true}
          onClick={() => onChange(true)}
          className={`rounded-lg border px-4 py-1.5 text-sm transition-colors focus:outline-none focus-visible:ring-2 focus-visible:ring-neutral-800 ${
            valor === true
              ? "border-neutral-800 bg-neutral-900 text-white"
              : "border-neutral-300 text-neutral-700 hover:border-neutral-400"
          }`}
        >
          Sim
        </button>
        <button
          type="button"
          role="radio"
          aria-checked={valor === false}
          onClick={() => onChange(false)}
          className={`rounded-lg border px-4 py-1.5 text-sm transition-colors focus:outline-none focus-visible:ring-2 focus-visible:ring-neutral-800 ${
            valor === false
              ? "border-neutral-800 bg-neutral-900 text-white"
              : "border-neutral-300 text-neutral-700 hover:border-neutral-400"
          }`}
        >
          Não
        </button>
      </div>
      {erro && (
        <p role="alert" className="mt-1 text-sm text-red-600">
          {erro}
        </p>
      )}
    </div>
  );
}
