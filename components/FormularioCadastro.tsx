"use client";

import { FormEvent, useId, useState } from "react";
import { cadastrarAluno, ErroApi, type NovoAluno } from "@/lib/api";
import { mascararCelular, mascararCpf } from "@/lib/masks";
import { validarCpf } from "@/lib/cpf";
import { somenteDigitos } from "@/lib/texto";
import CampoTexto from "./CampoTexto";

type CamposFormulario = {
  nome: string;
  cpf: string;
  celular: string;
  campus: string;
  aceiteLgpd: boolean;
};

type ErrosFormulario = Partial<Record<keyof CamposFormulario, string>>;

const CAMPOS_INICIAIS: CamposFormulario = {
  nome: "",
  cpf: "",
  celular: "",
  campus: "",
  aceiteLgpd: false,
};

type FormularioCadastroProps = {
  token: string;
  onCadastroConcluido: (nome: string) => void;
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

  if (campos.campus.trim().length === 0) {
    erros.campus = "Informe seu campus.";
  }

  if (!campos.aceiteLgpd) {
    erros.aceiteLgpd = "É necessário aceitar o uso dos dados para concluir o cadastro.";
  }

  return erros;
}

export default function FormularioCadastro({ token, onCadastroConcluido }: FormularioCadastroProps) {
  const [campos, setCampos] = useState<CamposFormulario>(CAMPOS_INICIAIS);
  const [erros, setErros] = useState<ErrosFormulario>({});
  const [alertaGeral, setAlertaGeral] = useState<string | null>(null);
  const [enviando, setEnviando] = useState(false);
  const idBase = useId();

  function atualizarCampo<CampoNome extends keyof CamposFormulario>(
    campo: CampoNome,
    valor: CamposFormulario[CampoNome],
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

    const dados: NovoAluno = {
      nome: campos.nome.trim(),
      cpf: somenteDigitos(campos.cpf),
      celular: somenteDigitos(campos.celular),
      campus: campos.campus.trim(),
      aceiteLgpd: campos.aceiteLgpd,
    };

    try {
      const aluno = await cadastrarAluno(token, dados);
      onCadastroConcluido(aluno.nome);
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
    <form onSubmit={aoEnviar} noValidate className="space-y-4">
      {alertaGeral && (
        <div role="alert" className="rounded-lg border border-red-300 bg-red-50 px-3 py-2 text-sm text-red-700">
          {alertaGeral}
        </div>
      )}

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
        inputMode="tel"
        autoComplete="tel"
        placeholder="(00) 00000-0000"
      />

      <CampoTexto
        id={`${idBase}-campus`}
        label="Campus"
        valor={campos.campus}
        onChange={(valor) => atualizarCampo("campus", valor)}
        erro={erros.campus}
        autoComplete="off"
        placeholder="Ex: Campus 85"
      />

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
            Li e concordo com o uso dos meus dados (nome, CPF, celular e campus) exclusivamente para
            controle de presença e identificação neste curso, conforme a LGPD.
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
        {enviando ? "Enviando..." : "Concluir cadastro"}
      </button>
    </form>
  );
}
