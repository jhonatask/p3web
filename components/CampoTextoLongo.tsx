type CampoTextoLongoProps = {
  id: string;
  label: string;
  valor: string;
  onChange: (valor: string) => void;
  erro?: string;
  placeholder?: string;
  linhas?: number;
};

export default function CampoTextoLongo({
  id,
  label,
  valor,
  onChange,
  erro,
  placeholder,
  linhas = 3,
}: CampoTextoLongoProps) {
  const idErro = `${id}-erro`;

  return (
    <div>
      <label htmlFor={id} className="block text-sm font-medium text-neutral-800">
        {label}
      </label>
      <textarea
        id={id}
        value={valor}
        onChange={(evento) => onChange(evento.target.value)}
        placeholder={placeholder}
        rows={linhas}
        aria-invalid={erro ? true : undefined}
        aria-describedby={erro ? idErro : undefined}
        className={`mt-1 w-full resize-y rounded-lg border px-3 py-2 text-base text-neutral-900 placeholder:text-neutral-400 focus:outline-none focus-visible:ring-2 focus-visible:ring-neutral-800 ${
          erro ? "border-red-400" : "border-neutral-300"
        }`}
      />
      {erro && (
        <p id={idErro} role="alert" className="mt-1 text-sm text-red-600">
          {erro}
        </p>
      )}
    </div>
  );
}
