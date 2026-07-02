export type TurmaPublica = {
  cursoNome: string;
  turmaNome: string;
  moduloAtual: string;
  status: string;
};

export type NovoAluno = {
  nome: string;
  cpf: string;
  celular: string;
  campus: string;
  aceiteLgpd: boolean;
};

export type AlunoCadastrado = {
  id: string;
  nome: string;
};

export type ErroCampo = {
  campo: string;
  mensagem: string;
};

export type ApiErrorBody = {
  mensagem: string;
  status: number;
  timestamp: string;
  erros: ErroCampo[] | null;
};

export class ErroApi extends Error {
  status: number;
  erros: ErroCampo[] | null;

  constructor(corpo: ApiErrorBody) {
    super(corpo.mensagem);
    this.name = "ErroApi";
    this.status = corpo.status;
    this.erros = corpo.erros;
  }
}

function obterUrlBase(): string {
  const url = process.env.NEXT_PUBLIC_API_URL;
  if (!url) {
    throw new Error("Variável de ambiente NEXT_PUBLIC_API_URL não configurada");
  }
  return url;
}

async function tratarResposta<T>(resposta: Response): Promise<T> {
  if (resposta.ok) {
    return (await resposta.json()) as T;
  }

  const corpo = (await resposta.json()) as ApiErrorBody;
  throw new ErroApi(corpo);
}

export async function buscarTurma(token: string): Promise<TurmaPublica> {
  const resposta = await fetch(`${obterUrlBase()}/api/publico/turmas/${token}`, {
    cache: "no-store",
  });
  return tratarResposta<TurmaPublica>(resposta);
}

export async function cadastrarAluno(token: string, dados: NovoAluno): Promise<AlunoCadastrado> {
  const resposta = await fetch(`${obterUrlBase()}/api/publico/turmas/${token}/alunos`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(dados),
  });
  return tratarResposta<AlunoCadastrado>(resposta);
}
