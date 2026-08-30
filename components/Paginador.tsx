type PaginadorProps = {
  pagina: number;
  totalPaginas: number;
  onMudarPagina: (pagina: number) => void;
};

export default function Paginador({ pagina, totalPaginas, onMudarPagina }: PaginadorProps) {
  if (totalPaginas <= 1) {
    return null;
  }

  const paginaAtual = Math.min(Math.max(pagina, 1), totalPaginas);

  return (
    <div className="flex items-center justify-between border-t border-neutral-200 px-4 py-3 text-sm">
      <button
        type="button"
        onClick={() => onMudarPagina(paginaAtual - 1)}
        disabled={paginaAtual <= 1}
        className="font-medium text-neutral-700 hover:underline disabled:text-neutral-300 disabled:no-underline"
      >
        Anterior
      </button>
      <span className="text-neutral-500">
        Página {paginaAtual} de {totalPaginas}
      </span>
      <button
        type="button"
        onClick={() => onMudarPagina(paginaAtual + 1)}
        disabled={paginaAtual >= totalPaginas}
        className="font-medium text-neutral-700 hover:underline disabled:text-neutral-300 disabled:no-underline"
      >
        Próxima
      </button>
    </div>
  );
}
