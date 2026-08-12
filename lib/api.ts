import { limparSessao, obterToken } from "./auth";

export type TurmaPublica = {
  cursoNome: string;
  turmaNome: string;
  moduloAtual: string;
  status: string;
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

/**
 * Injeta o token da area logada e trata 401 (sessao expirada/invalida)
 * mandando de volta pro login. Usado por todas as chamadas autenticadas da
 * area do lider.
 */
async function fetchAutenticado(caminho: string, opcoes: RequestInit = {}): Promise<Response> {
  const token = obterToken();
  const headers = new Headers(opcoes.headers);
  if (token) {
    headers.set("Authorization", `Bearer ${token}`);
  }

  const resposta = await fetch(`${obterUrlBase()}${caminho}`, { ...opcoes, headers, cache: "no-store" });

  if (resposta.status === 401) {
    limparSessao();
    if (typeof window !== "undefined") {
      window.location.href = "/login";
    }
  }

  return resposta;
}

export async function buscarTurma(token: string): Promise<TurmaPublica> {
  const resposta = await fetch(`${obterUrlBase()}/api/publico/turmas/${token}`, {
    cache: "no-store",
  });
  return tratarResposta<TurmaPublica>(resposta);
}

export type EstadoCivil = "SOLTEIRO" | "CASADO" | "DIVORCIADO" | "VIUVO";

export type TempoNaFamily = "MENOS_6_MESES" | "DE_6_MESES_A_1_ANO" | "DE_1_A_3_ANOS" | "MAIS_DE_3_ANOS";

export type ParticipanteCadastroRequest = {
  nome: string;
  cpf: string;
  dataNascimento: string;
  estadoCivil: EstadoCivil;
  temFilhos: boolean;
  quantidadeFilhos: number | null;
  cep: string;
  rua: string;
  numero: string;
  complemento: string;
  bairro: string;
  cidade: string;
  uf: string;
  pontoReferencia: string;
  tempoNaFamily: TempoNaFamily;
  batizado: boolean;
  serveMinisterio: boolean;
  qualMinisterioServe: string;
  instagram: string;
  facebook: string;
  cursosRealizados: string[];
  cursosOutros: string;
  eventosParticipou: string[];
  eventosOutros: string;
  participaGc: boolean;
  quaisGcs: string[];
  aceiteLgpd: boolean;
};

export type ParticipanteCadastrado = {
  id: string;
  nome: string;
  cpf: string;
};

export async function cadastrarParticipante(dados: ParticipanteCadastroRequest): Promise<ParticipanteCadastrado> {
  const resposta = await fetch(`${obterUrlBase()}/api/publico/participantes`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(dados),
  });
  return tratarResposta<ParticipanteCadastrado>(resposta);
}

export type ModuloAula = "INTRODUTORIO" | "PLATAFORMA" | "PROTECAO" | "PROVISAO";

export type AulaAtiva = {
  id: string;
  turmaId: string;
  tipoAulaId: string;
  modulo: ModuloAula;
  numero: number;
  tema: string;
  data: string;
  status: string;
};

export async function buscarAulaAtiva(token: string): Promise<AulaAtiva> {
  const resposta = await fetch(`${obterUrlBase()}/api/publico/turmas/${token}/aula-ativa`, {
    cache: "no-store",
  });
  return tratarResposta<AulaAtiva>(resposta);
}

export type ParticipanteResumo = {
  id: string;
  nome: string;
};

export async function buscarParticipantePorCpf(cpf: string): Promise<ParticipanteResumo> {
  const resposta = await fetch(`${obterUrlBase()}/api/publico/participantes/${cpf}/resumo`, {
    cache: "no-store",
  });
  return tratarResposta<ParticipanteResumo>(resposta);
}

export type OrigemPresenca = "CHAMADA" | "REPOSICAO";

export type Presenca = {
  id: string;
  participanteId: string;
  participanteNome: string;
  turmaId: string;
  aulaId: string;
  timestampCheckin: string;
  metodo: string;
  origem: OrigemPresenca;
  turmaOrigemNome: string | null;
};

export async function registrarChamada(token: string, cpf: string): Promise<Presenca> {
  const resposta = await fetch(`${obterUrlBase()}/api/publico/turmas/${token}/chamada`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ cpf }),
  });
  return tratarResposta<Presenca>(resposta);
}

export type DidaticaAvaliacao = "OTIMA" | "BOA" | "REGULAR" | "RUIM";

export type AvaliacaoRequest = {
  cpf: string;
  nota: number;
  didatica: DidaticaAvaliacao;
  sugestao: string;
  nps: number;
  desejaServir: boolean | null;
  habilidades: string | null;
};

export type Avaliacao = {
  id: string;
  participanteId: string;
  participanteNome: string;
  aulaId: string;
  nota: number;
  didatica: DidaticaAvaliacao;
  sugestao: string | null;
  nps: number;
  desejaServir: string | null;
  habilidades: string | null;
};

export async function registrarAvaliacao(aulaId: string, dados: AvaliacaoRequest): Promise<Avaliacao> {
  const resposta = await fetch(`${obterUrlBase()}/api/publico/aulas/${aulaId}/avaliacoes`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(dados),
  });
  return tratarResposta<Avaliacao>(resposta);
}

// ---------------------------------------------------------------------------
// Area logada do lider (autenticada)
// ---------------------------------------------------------------------------

export type Papel = "PROFESSOR" | "ADMIN" | "VISUALIZADOR";

export type LoginResponse = {
  token: string;
  nome: string;
  papel: Papel;
};

export async function login(email: string, senha: string): Promise<LoginResponse> {
  const resposta = await fetch(`${obterUrlBase()}/api/auth/login`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ email, senha }),
  });
  return tratarResposta<LoginResponse>(resposta);
}

export type Curso = {
  id: string;
  nome: string;
  descricao: string | null;
  ativo: boolean;
};

export async function listarCursos(ativo?: boolean): Promise<Curso[]> {
  const query = ativo === undefined ? "" : `?ativo=${ativo}`;
  const resposta = await fetchAutenticado(`/api/cursos${query}`);
  return tratarResposta<Curso[]>(resposta);
}

export async function listarTiposAulaDoCurso(cursoId: string): Promise<TipoAulaResponse[]> {
  const resposta = await fetchAutenticado(`/api/cursos/${cursoId}/tipos-aula`);
  return tratarResposta<TipoAulaResponse[]>(resposta);
}

export type StatusTurma = "ATIVA" | "ENCERRADA";

export type TurmaAdmin = {
  id: string;
  cursoId: string;
  cursoNome: string;
  nome: string;
  moduloAtual: string | null;
  tokenCadastro: string;
  linkCadastro: string;
  status: StatusTurma;
};

export type NovaTurma = {
  cursoId: string;
  nome: string;
  moduloAtual: string | null;
};

export async function criarTurma(dados: NovaTurma): Promise<TurmaAdmin> {
  const resposta = await fetchAutenticado("/api/turmas", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(dados),
  });
  return tratarResposta<TurmaAdmin>(resposta);
}

export async function listarTurmas(cursoId?: string, status?: StatusTurma): Promise<TurmaAdmin[]> {
  const parametros = new URLSearchParams();
  if (cursoId) parametros.set("cursoId", cursoId);
  if (status) parametros.set("status", status);
  const query = parametros.toString();
  const resposta = await fetchAutenticado(`/api/turmas${query ? `?${query}` : ""}`);
  return tratarResposta<TurmaAdmin[]>(resposta);
}

export async function buscarTurmaAdmin(turmaId: string): Promise<TurmaAdmin> {
  const resposta = await fetchAutenticado(`/api/turmas/${turmaId}`);
  return tratarResposta<TurmaAdmin>(resposta);
}

export async function encerrarTurma(turmaId: string): Promise<TurmaAdmin> {
  const resposta = await fetchAutenticado(`/api/turmas/${turmaId}/encerrar`, { method: "PATCH" });
  return tratarResposta<TurmaAdmin>(resposta);
}

export async function baixarQrCodeTurma(turmaId: string): Promise<Blob> {
  const resposta = await fetchAutenticado(`/api/turmas/${turmaId}/qrcode`);
  if (!resposta.ok) {
    const corpo = (await resposta.json()) as ApiErrorBody;
    throw new ErroApi(corpo);
  }
  return resposta.blob();
}

export type TipoAulaResponse = {
  id: string;
  modulo: ModuloAula;
  numero: number;
  tema: string;
  ordem: number;
};

export type AulaAdmin = {
  id: string;
  turmaId: string;
  tipoAulaId: string;
  modulo: ModuloAula;
  numero: number;
  tema: string;
  data: string;
  status: string;
};

export type ItemAgenda = {
  tipoAulaId: string;
  data: string;
};

export async function definirAgenda(turmaId: string, itens: ItemAgenda[]): Promise<AulaAdmin[]> {
  const resposta = await fetchAutenticado(`/api/turmas/${turmaId}/agenda`, {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ itens }),
  });
  return tratarResposta<AulaAdmin[]>(resposta);
}

export async function listarAulasDaTurma(turmaId: string): Promise<AulaAdmin[]> {
  const resposta = await fetchAutenticado(`/api/turmas/${turmaId}/aulas`);
  return tratarResposta<AulaAdmin[]>(resposta);
}

export async function encerrarAulaAtual(turmaId: string): Promise<AulaAdmin | null> {
  const resposta = await fetchAutenticado(`/api/turmas/${turmaId}/agenda/atual/encerrar`, { method: "PATCH" });
  if (resposta.status === 204) {
    return null;
  }
  return tratarResposta<AulaAdmin>(resposta);
}

/** Encerra a aula ativa SEM abrir a proxima - fica sem nenhuma aula ativa. */
export async function desativarAulaAtual(turmaId: string): Promise<void> {
  const resposta = await fetchAutenticado(`/api/turmas/${turmaId}/agenda/atual/desativar`, { method: "PATCH" });
  if (!resposta.ok) {
    const corpo = (await resposta.json()) as ApiErrorBody;
    throw new ErroApi(corpo);
  }
}

export type StatusGeralFormatura = "APTO_COMPLETO" | "APTO_REPOR_RECOMENDADO" | "NAO_FORMA";

export type AulaFalta = {
  tipoAulaId: string;
  numero: number;
  tema: string;
};

export type StatusModuloFormatura = {
  modulo: ModuloAula;
  totalAulas: number;
  faltas: AulaFalta[];
  reprovado: boolean;
};

export type StatusFormatura = {
  participanteId: string;
  modulos: StatusModuloFormatura[];
  apto: boolean;
  aRepor: AulaFalta[];
  statusGeral: StatusGeralFormatura;
};

export type PainelLinha = {
  participanteId: string;
  nome: string;
  formatura: StatusFormatura;
};

export type PainelTurma = {
  turmaId: string;
  turmaNome: string | null;
  linhas: PainelLinha[];
};

export async function obterPainel(turmaId: string): Promise<PainelTurma> {
  const resposta = await fetchAutenticado(`/api/turmas/${turmaId}/painel`);
  return tratarResposta<PainelTurma>(resposta);
}

export async function gerarLinkWhatsapp(turmaId: string, participanteId: string): Promise<{ link: string }> {
  const resposta = await fetchAutenticado(`/api/turmas/${turmaId}/painel/${participanteId}/link-whatsapp`);
  return tratarResposta<{ link: string }>(resposta);
}

export type AlunoAdmin = {
  id: string;
  participanteId: string;
  nome: string;
  cpf: string;
  celular: string | null;
  campus: string | null;
  turmaId: string;
  dataMatricula: string;
};

export async function listarAlunosDaTurma(turmaId: string): Promise<AlunoAdmin[]> {
  const resposta = await fetchAutenticado(`/api/turmas/${turmaId}/alunos`);
  return tratarResposta<AlunoAdmin[]>(resposta);
}
