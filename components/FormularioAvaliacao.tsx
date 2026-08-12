"use client";

import { FormEvent, useId, useState } from "react";
import { registrarAvaliacao, ErroApi, type AulaAtiva, type DidaticaAvaliacao, type OrigemPresenca } from "@/lib/api";
import CampoEscala from "./CampoEscala";
import CampoSelecaoUnica, { type OpcaoSelecao } from "./CampoSelecaoUnica";
import CampoTextoLongo from "./CampoTextoLongo";
import CampoSimNao from "./CampoSimNao";

const OPCOES_DIDATICA: OpcaoSelecao<DidaticaAvaliacao>[] = [
  { valor: "OTIMA", rotulo: "Excelente" },
  { valor: "BOA", rotulo: "Bom" },
  { valor: "REGULAR", rotulo: "Regular" },
  { valor: "RUIM", rotulo: "Ruim" },
];

// 3a aula de cada modulo (Confianca, Sacerdocio, O Plano) - momento em que o
// mapa pede para perguntar sobre despertar para servir.
const NUMEROS_AULA_DESPERTAR_PARA_SERVIR = [3, 6, 9];

type CamposAvaliacao = {
  nota: number | null;
  didatica: DidaticaAvaliacao | null;
  sugestao: string;
  nps: number | null;
  desejaServir: boolean | null;
  habilidades: string;
};

type ErrosAvaliacao = Partial<Record<keyof CamposAvaliacao, string>>;

const CAMPOS_INICIAIS: CamposAvaliacao = {
  nota: null,
  didatica: null,
  sugestao: "",
  nps: null,
  desejaServir: null,
  habilidades: "",
};

type FormularioAvaliacaoProps = {
  aulaAtiva: AulaAtiva;
  cpf: string;
  nome: string;
  origem: OrigemPresenca;
  turmaOrigemNome: string | null;
  onConcluido: () => void;
};

function validarCampos(campos: CamposAvaliacao): ErrosAvaliacao {
  const erros: ErrosAvaliacao = {};
  if (campos.nota === null) {
    erros.nota = "Dê uma nota de 0 a 10 para a aula.";
  }
  if (!campos.didatica) {
    erros.didatica = "Selecione uma opção.";
  }
  if (campos.nps === null) {
    erros.nps = "Informe de 0 a 10 a chance de recomendar a Escola.";
  }
  return erros;
}

export default function FormularioAvaliacao({
  aulaAtiva,
  cpf,
  nome,
  origem,
  turmaOrigemNome,
  onConcluido,
}: FormularioAvaliacaoProps) {
  const [campos, setCampos] = useState<CamposAvaliacao>(CAMPOS_INICIAIS);
  const [erros, setErros] = useState<ErrosAvaliacao>({});
  const [alertaGeral, setAlertaGeral] = useState<string | null>(null);
  const [enviando, setEnviando] = useState(false);
  const idBase = useId();
  const primeiroNome = nome.trim().split(/\s+/)[0];
  const perguntaServir = NUMEROS_AULA_DESPERTAR_PARA_SERVIR.includes(aulaAtiva.numero);

  function atualizarCampo<CampoNome extends keyof CamposAvaliacao>(
    campo: CampoNome,
    valor: CamposAvaliacao[CampoNome],
  ) {
    setCampos((atual) => ({ ...atual, [campo]: valor }));
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

    try {
      await registrarAvaliacao(aulaAtiva.id, {
        cpf,
        nota: campos.nota as number,
        didatica: campos.didatica as DidaticaAvaliacao,
        sugestao: campos.sugestao.trim(),
        nps: campos.nps as number,
        desejaServir: perguntaServir ? campos.desejaServir : null,
        habilidades: perguntaServir ? campos.habilidades.trim() || null : null,
      });
      onConcluido();
    } catch (erro) {
      if (erro instanceof ErroApi) {
        setAlertaGeral(erro.message);
      } else {
        setAlertaGeral("Não foi possível enviar sua avaliação. Verifique sua conexão e tente novamente.");
      }
    } finally {
      setEnviando(false);
    }
  }

  return (
    <div>
      <div className="mb-6 text-center">
        <p className="text-sm text-neutral-500">Olá, {primeiroNome}</p>
        <h1 className="mt-1 text-xl font-semibold text-neutral-900">Chamada da aula: {aulaAtiva.tema}</h1>
        {origem === "REPOSICAO" && (
          <p className="mt-3 rounded-lg border border-amber-300 bg-amber-50 px-3 py-2 text-sm text-amber-800">
            Isso conta como reposição da aula {aulaAtiva.tema}
            {turmaOrigemNome ? ` — sua turma é ${turmaOrigemNome}` : ""}.
          </p>
        )}
      </div>

      <form onSubmit={aoEnviar} noValidate className="space-y-6">
        {alertaGeral && (
          <div role="alert" className="rounded-lg border border-red-300 bg-red-50 px-3 py-2 text-sm text-red-700">
            {alertaGeral}
          </div>
        )}

        <CampoEscala
          label="Como você avalia a aula de hoje? 0 a 10"
          valor={campos.nota}
          onChange={(valor) => atualizarCampo("nota", valor)}
          erro={erros.nota}
        />

        <CampoSelecaoUnica
          label="Didática / professor"
          opcoes={OPCOES_DIDATICA}
          valor={campos.didatica}
          onChange={(valor) => atualizarCampo("didatica", valor)}
          erro={erros.didatica}
        />

        <CampoTextoLongo
          id={`${idBase}-sugestao`}
          label="O que podemos melhorar?"
          valor={campos.sugestao}
          onChange={(valor) => atualizarCampo("sugestao", valor)}
          placeholder="Críticas, sugestões e elogios (opcional)"
        />

        <CampoEscala
          label="Recomendaria a Escola? NPS · 0 a 10"
          valor={campos.nps}
          onChange={(valor) => atualizarCampo("nps", valor)}
          erro={erros.nps}
        />

        {perguntaServir && (
          <div className="space-y-4 rounded-lg border border-amber-200 bg-amber-50 p-4">
            <p className="text-xs font-medium uppercase tracking-wide text-amber-700">
              Só na 3ª aula de cada módulo · vai para a sua base de cadastro
            </p>
            <CampoSimNao
              label="Deseja servir em algum ministério?"
              valor={campos.desejaServir}
              onChange={(valor) => atualizarCampo("desejaServir", valor)}
            />
            <CampoTextoLongo
              id={`${idBase}-habilidades`}
              label="O que você sabe fazer?"
              valor={campos.habilidades}
              onChange={(valor) => atualizarCampo("habilidades", valor)}
              placeholder="Habilidades (opcional)"
            />
          </div>
        )}

        <button
          type="submit"
          disabled={enviando}
          className="w-full rounded-lg bg-neutral-900 px-4 py-3 text-base font-medium text-white transition-colors focus:outline-none focus-visible:ring-2 focus-visible:ring-neutral-800 focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:bg-neutral-300"
        >
          {enviando ? "Enviando..." : "Enviar chamada"}
        </button>
      </form>
    </div>
  );
}
