export const ITENS_POR_PAGINA = 5;

export function totalDePaginas(totalItens: number, tamanhoPagina: number = ITENS_POR_PAGINA): number {
  return Math.max(1, Math.ceil(totalItens / tamanhoPagina));
}

export function paginar<T>(itens: T[], pagina: number, tamanhoPagina: number = ITENS_POR_PAGINA): T[] {
  const paginaValida = Math.min(Math.max(pagina, 1), totalDePaginas(itens.length, tamanhoPagina));
  const inicio = (paginaValida - 1) * tamanhoPagina;
  return itens.slice(inicio, inicio + tamanhoPagina);
}
