type CampoTextoProps = {
  id: string;
  label: string;
  valor: string;
  onChange: (valor: string) => void;
  erro?: string;
  tipo?: string;
  autoComplete?: string;
  placeholder?: string;
  inputMode?: "text" | "numeric" | "tel";
};

export default function CampoTexto({
  id,
  label,
  valor,
  onChange,
  erro,
  tipo = "text",
  autoComplete,
  placeholder,
  inputMode,
}: CampoTextoProps) {
  const idErro = `${id}-erro`;

  return (
    <div>
      <label htmlFor={id} className="block text-sm font-medium text-neutral-800">
        {label}
      </label>
      <input
        id={id}
        type={tipo}
        value={valor}
        onChange={(evento) => onChange(evento.target.value)}
        autoComplete={autoComplete}
        placeholder={placeholder}
        inputMode={inputMode}
        aria-invalid={erro ? true : undefined}
        aria-describedby={erro ? idErro : undefined}
        className={`mt-1 w-full rounded-lg border px-3 py-2 text-base text-neutral-900 placeholder:text-neutral-400 focus:outline-none focus-visible:ring-2 focus-visible:ring-neutral-800 ${
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
