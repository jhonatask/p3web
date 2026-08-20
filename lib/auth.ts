const CHAVE_TOKEN = "p3_token";
const CHAVE_NOME = "p3_nome";

export function salvarSessao(token: string, nome: string): void {
  localStorage.setItem(CHAVE_TOKEN, token);
  localStorage.setItem(CHAVE_NOME, nome);
}

export function obterToken(): string | null {
  if (typeof window === "undefined") {
    return null;
  }
  return localStorage.getItem(CHAVE_TOKEN);
}

export function obterNomeLogado(): string | null {
  if (typeof window === "undefined") {
    return null;
  }
  return localStorage.getItem(CHAVE_NOME);
}

export function limparSessao(): void {
  localStorage.removeItem(CHAVE_TOKEN);
  localStorage.removeItem(CHAVE_NOME);
}

export function estaAutenticado(): boolean {
  return obterToken() !== null;
}
