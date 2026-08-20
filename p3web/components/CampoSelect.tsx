type OpcaoCampoSelect = {
  valor: string;
  rotulo: string;
};

type CampoSelectProps = {
  id: string;
  label: string;
  valor: string;
  onChange: (valor: string) => void;
  opcoes: OpcaoCampoSelect[];
  erro?: string;
  placeholder?: string;
};

export default function CampoSelect({ id, label, valor, onChange, opcoes, erro, placeholder }: CampoSelectProps) {
  const idErro = `${id}-erro`;

  return (
    <div>
      <label htmlFor={id} className="block text-sm font-medium text-neutral-800">
        {label}
      </label>
      <select
        id={id}
        value={valor}
        onChange={(evento) => onChange(evento.target.value)}
        aria-invalid={erro ? true : undefined}
        aria-describedby={erro ? idErro : undefined}
        className={`mt-1 w-full rounded-lg border bg-white px-3 py-2 text-base text-neutral-900 focus:outline-none focus-visible:ring-2 focus-visible:ring-neutral-800 ${
          erro ? "border-red-400" : "border-neutral-300"
        }`}
      >
        {placeholder && <option value="">{placeholder}</option>}
        {opcoes.map((opcao) => (
          <option key={opcao.valor} value={opcao.valor}>
            {opcao.rotulo}
          </option>
        ))}
      </select>
      {erro && (
        <p id={idErro} role="alert" className="mt-1 text-sm text-red-600">
          {erro}
        </p>
      )}
    </div>
  );
}
