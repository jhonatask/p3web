"use client";

import { FormEvent, useId, useState } from "react";
import {
  cadastrarParticipante,
  ErroApi,
  type EstadoCivil,
  type ParticipanteCadastrado,
  type ParticipanteCadastroRequest,
  type TempoNaFamily,
} from "@/lib/api";
import { mascararCelular, mascararCep, mascararCpf } from "@/lib/masks";
import { validarCpf } from "@/lib/cpf";
import { somenteDigitos } from "@/lib/texto";
import { buscarEnderecoPorCep } from "@/lib/viacep";
import CampoTexto from "./CampoTexto";
import CampoSelecaoUnica, { type OpcaoSelecao } from "./CampoSelecaoUnica";
import CampoMultiplaEscolha from "./CampoMultiplaEscolha";
import CampoSimNao from "./CampoSimNao";

const OPCOES_ESTADO_CIVIL: OpcaoSelecao<EstadoCivil>[] = [
  { valor: "SOLTEIRO", rotulo: "Solteiro" },
  { valor: "CASADO", rotulo: "Casado" },
  { valor: "DIVORCIADO", rotulo: "Divorciado" },
  { valor: "VIUVO", rotulo: "Viúvo" },
];

const OPCOES_TEMPO_NA_FAMILY: OpcaoSelecao<TempoNaFamily>[] = [
  { valor: "MENOS_6_MESES", rotulo: "Menos de 6 meses" },
  { valor: "DE_6_MESES_A_1_ANO", rotulo: "6 meses a 1 ano" },
  { valor: "DE_1_A_3_ANOS", rotulo: "1 a 3 anos" },
  { valor: "MAIS_DE_3_ANOS", rotulo: "Mais de 3 anos" },
];

const CURSOS_DISPONIVEIS = [
  "Primeiros Passos",
  "Hombridade",
  "Homem ao Máximo",
  "Curso de Casais",
  "Curso de Servos",
  "Curso de Líderes",
  "Estudo Bíblico",
];

const EVENTOS_DISPONIVEIS = ["Awake", "Forja", "Conferência dos Homens"];

const GCS_DISPONIVEIS = ["GC de Casais", "GC de Homens"];

type CamposFormulario = {
  nome: string;
  cpf: string;
  celular: string;
  dataNascimento: string;
  estadoCivil: EstadoCivil | null;
  temFilhos: boolean | null;
  quantidadeFilhos: string;
  cep: string;
  rua: string;
  numero: string;
  complemento: string;
  bairro: string;
  cidade: string;
  uf: string;
  pontoReferencia: string;
  tempoNaFamily: TempoNaFamily | null;
  batizado: boolean | null;
  serveMinisterio: boolean | null;
  qualMinisterioServe: string;
  instagram: string;
  facebook: string;
  cursosRealizados: string[];
  cursosOutros: string;
  eventosParticipou: string[];
  eventosOutros: string;
  participaGc: boolean | null;
  quaisGcs: string[];
  aceiteLgpd: boolean;
};

type ErrosFormulario = Partial<Record<keyof CamposFormulario, string>>;

const CAMPOS_INICIAIS: CamposFormulario = {
  nome: "",
  cpf: "",
  celular: "",
  dataNascimento: "",
  estadoCivil: null,
  temFilhos: null,
  quantidadeFilhos: "",
  cep: "",
  rua: "",
  numero: "",
  complemento: "",
  bairro: "",
  cidade: "",
  uf: "",
  pontoReferencia: "",
  tempoNaFamily: null,
  batizado: null,
  serveMinisterio: null,
  qualMinisterioServe: "",
  instagram: "",
  facebook: "",
  cursosRealizados: [],
  cursosOutros: "",
  eventosParticipou: [],
  eventosOutros: "",
  participaGc: null,
  quaisGcs: [],
  aceiteLgpd: false,
};

type FormularioCadastroBaseProps = {
  onCadastroConcluido: (participante: ParticipanteCadastrado) => void;
  cpfInicial?: string;
};

function validarCampos(campos: CamposFormulario): ErrosFormulario {
  const erros: ErrosFormulario = {};

  if (campos.nome.trim().length === 0) {
    erros.nome = "Informe seu nome completo.";
  }

  if (!validarCpf(campos.cpf)) {
    erros.cpf = "CPF inválido. Confira os números digitados.";
  }

  if (somenteDigitos(campos.celular).length < 10) {
    erros.celular = "Informe um celular válido, com DDD.";
  }

  if (!campos.dataNascimento) {
    erros.dataNascimento = "Informe sua data de nascimento.";
  }

  if (!campos.estadoCivil) {
    erros.estadoCivil = "Selecione seu estado civil.";
  }

  if (campos.temFilhos === null) {
    erros.temFilhos = "Informe se você tem filhos.";
  }

  if (campos.temFilhos === true && campos.quantidadeFilhos.trim().length === 0) {
    erros.quantidadeFilhos = "Informe a quantidade de filhos.";
  }

  if (somenteDigitos(campos.cep).length !== 8) {
    erros.cep = "Informe um CEP válido.";
  }

  if (campos.bairro.trim().length === 0) {
    erros.bairro = "Informe o bairro.";
  }

  if (campos.cidade.trim().length === 0) {
    erros.cidade = "Informe a cidade.";
  }

  if (campos.uf.trim().length !== 2) {
    erros.uf = "Informe a UF (2 letras).";
  }

  if (!campos.tempoNaFamily) {
    erros.tempoNaFamily = "Informe há quanto tempo você está na Family.";
  }

  if (campos.batizado === null) {
    erros.batizado = "Informe se você é batizado.";
  }

  if (campos.serveMinisterio === null) {
    erros.serveMinisterio = "Informe se você já serve em algum ministério.";
  }

  if (campos.participaGc === null) {
    erros.participaGc = "Informe se você participa de algum GC.";
  }

  if (campos.participaGc === true && campos.quaisGcs.length === 0) {
    erros.quaisGcs = "Selecione ao menos um GC.";
  }

  if (!campos.aceiteLgpd) {
    erros.aceiteLgpd = "É necessário aceitar o uso dos dados para concluir o cadastro.";
  }

  return erros;
}

export default function FormularioCadastroBase({ onCadastroConcluido, cpfInicial }: FormularioCadastroBaseProps) {
  const [campos, setCampos] = useState<CamposFormulario>(() =>
    cpfInicial ? { ...CAMPOS_INICIAIS, cpf: mascararCpf(cpfInicial) } : CAMPOS_INICIAIS,
  );
  const [erros, setErros] = useState<ErrosFormulario>({});
  const [alertaGeral, setAlertaGeral] = useState<string | null>(null);
  const [enviando, setEnviando] = useState(false);
  const [buscandoCep, setBuscandoCep] = useState(false);
  const idBase = useId();

  function atualizarCampo<CampoNome extends keyof CamposFormulario>(
    campo: CampoNome,
    valor: CamposFormulario[CampoNome],
  ) {
    setCampos((atual) => ({ ...atual, [campo]: valor }));
  }

  async function aoSairDoCep() {
    const digitos = somenteDigitos(campos.cep);
    if (digitos.length !== 8) {
      return;
    }

    setBuscandoCep(true);
    const endereco = await buscarEnderecoPorCep(campos.cep);
    setBuscandoCep(false);

    if (!endereco) {
      return;
    }

    setCampos((atual) => ({
      ...atual,
      rua: endereco.logradouro || atual.rua,
      bairro: endereco.bairro || atual.bairro,
      cidade: endereco.localidade || atual.cidade,
      uf: endereco.uf || atual.uf,
    }));
  }

  async function aoEnviar(evento: FormEvent<HTMLFormElement>) {
    evento.preventDefault();
    setAlertaGeral(null);

    const errosDeValidacao = validarCampos(campos);
    if (Object.keys(errosDeValidacao).length > 0) {
      setErros(errosDeValidacao);
      return;
    }

    setErros({});
    setEnviando(true);

    const dados: ParticipanteCadastroRequest = {
      nome: campos.nome.trim(),
      cpf: somenteDigitos(campos.cpf),
      celular: somenteDigitos(campos.celular),
      dataNascimento: campos.dataNascimento,
      estadoCivil: campos.estadoCivil as EstadoCivil,
      temFilhos: campos.temFilhos === true,
      quantidadeFilhos: campos.temFilhos === true ? Number(campos.quantidadeFilhos) : null,
      cep: somenteDigitos(campos.cep),
      rua: campos.rua.trim(),
      numero: campos.numero.trim(),
      complemento: campos.complemento.trim(),
      bairro: campos.bairro.trim(),
      cidade: campos.cidade.trim(),
      uf: campos.uf.trim().toUpperCase(),
      pontoReferencia: campos.pontoReferencia.trim(),
      tempoNaFamily: campos.tempoNaFamily as TempoNaFamily,
      batizado: campos.batizado === true,
      serveMinisterio: campos.serveMinisterio === true,
      qualMinisterioServe: campos.qualMinisterioServe.trim(),
      instagram: campos.instagram.trim(),
      facebook: campos.facebook.trim(),
      cursosRealizados: campos.cursosRealizados,
      cursosOutros: campos.cursosOutros.trim(),
      eventosParticipou: campos.eventosParticipou,
      eventosOutros: campos.eventosOutros.trim(),
      participaGc: campos.participaGc === true,
      quaisGcs: campos.quaisGcs,
      aceiteLgpd: campos.aceiteLgpd,
    };

    try {
      const participante = await cadastrarParticipante(dados);
      onCadastroConcluido(participante);
    } catch (erro) {
      if (erro instanceof ErroApi && erro.erros && erro.erros.length > 0) {
        const errosDeCampo: ErrosFormulario = {};
        for (const item of erro.erros) {
          errosDeCampo[item.campo as keyof CamposFormulario] = item.mensagem;
        }
        setErros(errosDeCampo);
      } else if (erro instanceof ErroApi) {
        setAlertaGeral(erro.message);
      } else {
        setAlertaGeral("Não foi possível enviar seu cadastro. Verifique sua conexão e tente novamente.");
      }
    } finally {
      setEnviando(false);
    }
  }

  return (
    <form onSubmit={aoEnviar} noValidate className="space-y-8">
      {alertaGeral && (
        <div role="alert" className="rounded-lg border border-red-300 bg-red-50 px-3 py-2 text-sm text-red-700">
          {alertaGeral}
        </div>
      )}

      <section className="space-y-4">
        <h2 className="text-sm font-semibold uppercase tracking-wide text-neutral-500">Identidade</h2>

        <CampoTexto
          id={`${idBase}-nome`}
          label="Nome completo"
          valor={campos.nome}
          onChange={(valor) => atualizarCampo("nome", valor)}
          erro={erros.nome}
          autoComplete="name"
          placeholder="Seu nome completo"
        />

        <CampoTexto
          id={`${idBase}-cpf`}
          label="CPF"
          valor={campos.cpf}
          onChange={(valor) => atualizarCampo("cpf", mascararCpf(valor))}
          erro={erros.cpf}
          inputMode="numeric"
          autoComplete="off"
          placeholder="000.000.000-00"
        />

        <CampoTexto
          id={`${idBase}-celular`}
          label="Celular"
          valor={campos.celular}
          onChange={(valor) => atualizarCampo("celular", mascararCelular(valor))}
          erro={erros.celular}
          inputMode="numeric"
          autoComplete="tel"
          placeholder="(00) 00000-0000"
        />

        <CampoTexto
          id={`${idBase}-data-nascimento`}
          label="Data de nascimento"
          tipo="date"
          valor={campos.dataNascimento}
          onChange={(valor) => atualizarCampo("dataNascimento", valor)}
          erro={erros.dataNascimento}
          autoComplete="bday"
        />

        <CampoSelecaoUnica
          label="Estado civil"
          opcoes={OPCOES_ESTADO_CIVIL}
          valor={campos.estadoCivil}
          onChange={(valor) => atualizarCampo("estadoCivil", valor)}
          erro={erros.estadoCivil}
        />

        <CampoSimNao
          label="Tem filhos?"
          valor={campos.temFilhos}
          onChange={(valor) => atualizarCampo("temFilhos", valor)}
          erro={erros.temFilhos}
        />

        {campos.temFilhos === true && (
          <CampoTexto
            id={`${idBase}-quantidade-filhos`}
            label="Quantos filhos?"
            tipo="number"
            valor={campos.quantidadeFilhos}
            onChange={(valor) => atualizarCampo("quantidadeFilhos", valor)}
            erro={erros.quantidadeFilhos}
            inputMode="numeric"
          />
        )}
      </section>

      <section className="space-y-4">
        <h2 className="text-sm font-semibold uppercase tracking-wide text-neutral-500">
          Endereço <span className="font-normal normal-case text-neutral-400">define sua região</span>
        </h2>

        <CampoTexto
          id={`${idBase}-cep`}
          label={buscandoCep ? "CEP (buscando endereço...)" : "CEP"}
          valor={campos.cep}
          onChange={(valor) => atualizarCampo("cep", mascararCep(valor))}
          onBlur={aoSairDoCep}
          erro={erros.cep}
          inputMode="numeric"
          autoComplete="postal-code"
          placeholder="00000-000"
        />

        <CampoTexto
          id={`${idBase}-rua`}
          label="Rua"
          valor={campos.rua}
          onChange={(valor) => atualizarCampo("rua", valor)}
          autoComplete="address-line1"
          placeholder="Preenchido pelo CEP (editável)"
        />

        <div className="grid grid-cols-2 gap-3">
          <CampoTexto
            id={`${idBase}-numero`}
            label="Número"
            valor={campos.numero}
            onChange={(valor) => atualizarCampo("numero", valor)}
            placeholder="Opcional"
          />
          <CampoTexto
            id={`${idBase}-complemento`}
            label="Complemento"
            valor={campos.complemento}
            onChange={(valor) => atualizarCampo("complemento", valor)}
            placeholder="Quadra, lote, apto..."
          />
        </div>

        <CampoTexto
          id={`${idBase}-bairro`}
          label="Bairro"
          valor={campos.bairro}
          onChange={(valor) => atualizarCampo("bairro", valor)}
          erro={erros.bairro}
          autoComplete="address-level3"
          placeholder="Preenchido pelo CEP (editável)"
        />

        <div className="grid grid-cols-3 gap-3">
          <div className="col-span-2">
            <CampoTexto
              id={`${idBase}-cidade`}
              label="Cidade"
              valor={campos.cidade}
              onChange={(valor) => atualizarCampo("cidade", valor)}
              erro={erros.cidade}
              autoComplete="address-level2"
            />
          </div>
          <CampoTexto
            id={`${idBase}-uf`}
            label="UF"
            valor={campos.uf}
            onChange={(valor) => atualizarCampo("uf", valor.toUpperCase().slice(0, 2))}
            erro={erros.uf}
            autoComplete="address-level1"
          />
        </div>

        <CampoTexto
          id={`${idBase}-ponto-referencia`}
          label="Ponto de referência"
          valor={campos.pontoReferencia}
          onChange={(valor) => atualizarCampo("pontoReferencia", valor)}
          placeholder="Opcional"
        />
      </section>

      <section className="space-y-4">
        <h2 className="text-sm font-semibold uppercase tracking-wide text-neutral-500">Vida na Family</h2>

        <CampoSelecaoUnica
          label="Quanto tempo na Family?"
          opcoes={OPCOES_TEMPO_NA_FAMILY}
          valor={campos.tempoNaFamily}
          onChange={(valor) => atualizarCampo("tempoNaFamily", valor)}
          erro={erros.tempoNaFamily}
        />

        <CampoSimNao
          label="É batizado?"
          valor={campos.batizado}
          onChange={(valor) => atualizarCampo("batizado", valor)}
          erro={erros.batizado}
        />

        <CampoSimNao
          label="Já serve em algum ministério na Family?"
          valor={campos.serveMinisterio}
          onChange={(valor) => atualizarCampo("serveMinisterio", valor)}
          erro={erros.serveMinisterio}
        />

        {campos.serveMinisterio === true && (
          <CampoTexto
            id={`${idBase}-qual-ministerio`}
            label="Qual(is)?"
            valor={campos.qualMinisterioServe}
            onChange={(valor) => atualizarCampo("qualMinisterioServe", valor)}
            placeholder="Opcional"
          />
        )}

        <CampoTexto
          id={`${idBase}-instagram`}
          label="Instagram"
          valor={campos.instagram}
          onChange={(valor) => atualizarCampo("instagram", valor)}
          placeholder="@usuario (opcional)"
        />

        <CampoTexto
          id={`${idBase}-facebook`}
          label="Facebook"
          valor={campos.facebook}
          onChange={(valor) => atualizarCampo("facebook", valor)}
          placeholder="Usuário ou link (opcional)"
        />
      </section>

      <section className="space-y-4">
        <h2 className="text-sm font-semibold uppercase tracking-wide text-neutral-500">
          História na Family <span className="font-normal normal-case text-neutral-400">só o que foi feito na Family</span>
        </h2>

        <CampoMultiplaEscolha
          label="Cursos realizados na Family"
          descricao="opcional"
          opcoes={CURSOS_DISPONIVEIS}
          valores={campos.cursosRealizados}
          onChange={(valores) => atualizarCampo("cursosRealizados", valores)}
          erro={erros.cursosRealizados}
        />

        <CampoTexto
          id={`${idBase}-cursos-outros`}
          label="Cursos — Outros"
          valor={campos.cursosOutros}
          onChange={(valor) => atualizarCampo("cursosOutros", valor)}
          placeholder="Algum curso que não está na lista? (opcional)"
        />

        <CampoMultiplaEscolha
          label="Eventos que participou na Family"
          descricao="opcional"
          opcoes={EVENTOS_DISPONIVEIS}
          valores={campos.eventosParticipou}
          onChange={(valores) => atualizarCampo("eventosParticipou", valores)}
          erro={erros.eventosParticipou}
        />

        <CampoTexto
          id={`${idBase}-eventos-outros`}
          label="Eventos — Outros"
          valor={campos.eventosOutros}
          onChange={(valor) => atualizarCampo("eventosOutros", valor)}
          placeholder="Algum evento que não está na lista? (opcional)"
        />
      </section>

      <section className="space-y-4">
        <h2 className="text-sm font-semibold uppercase tracking-wide text-neutral-500">Comunhão</h2>

        <CampoSimNao
          label="Participa de algum GC?"
          valor={campos.participaGc}
          onChange={(valor) => atualizarCampo("participaGc", valor)}
          erro={erros.participaGc}
        />

        {campos.participaGc === true && (
          <CampoMultiplaEscolha
            label="Quais GCs?"
            opcoes={GCS_DISPONIVEIS}
            valores={campos.quaisGcs}
            onChange={(valores) => atualizarCampo("quaisGcs", valores)}
            erro={erros.quaisGcs}
          />
        )}
      </section>

      <div>
        <div className="flex items-start gap-3">
          <input
            id={`${idBase}-lgpd`}
            type="checkbox"
            checked={campos.aceiteLgpd}
            onChange={(evento) => atualizarCampo("aceiteLgpd", evento.target.checked)}
            aria-describedby={erros.aceiteLgpd ? `${idBase}-lgpd-erro` : undefined}
            className="mt-1 h-4 w-4 shrink-0 rounded border-neutral-400 text-neutral-800 focus:outline-none focus-visible:ring-2 focus-visible:ring-neutral-800"
          />
          <label htmlFor={`${idBase}-lgpd`} className="text-sm text-neutral-700">
            Li e concordo com o uso dos meus dados para fins de identificação, comunicação e acompanhamento
            nos ministérios da Family, conforme a LGPD.
          </label>
        </div>
        {erros.aceiteLgpd && (
          <p id={`${idBase}-lgpd-erro`} role="alert" className="mt-1 text-sm text-red-600">
            {erros.aceiteLgpd}
          </p>
        )}
      </div>

      <button
        type="submit"
        disabled={!campos.aceiteLgpd || enviando}
        className="w-full rounded-lg bg-neutral-900 px-4 py-3 text-base font-medium text-white transition-colors focus:outline-none focus-visible:ring-2 focus-visible:ring-neutral-800 focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:bg-neutral-300"
      >
        {enviando ? "Enviando..." : "Salvar cadastro"}
      </button>
    </form>
  );
}
