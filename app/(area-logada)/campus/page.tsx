"use client";

import { FormEvent, useEffect, useState } from "react";
import { listarCampus, criarCampus, atualizarCampus, desativarCampus, ErroApi, type Campus } from "@/lib/api";
import CampoTexto from "@/components/CampoTexto";
import { IconeCheckCirculo, IconeLapis, IconeProibir, IconeUsuarios } from "@/components/Icones";

export default function PaginaCampus() {
  const [campi, setCampi] = useState<Campus[] | null>(null);
  const [erro, setErro] = useState<string | null>(null);
  const [nomeNovo, setNomeNovo] = useState("");
  const [enviando, setEnviando] = useState(false);
  const [editandoId, setEditandoId] = useState<string | null>(null);
  const [nomeEdicao, setNomeEdicao] = useState("");

  async function carregar() {
    try {
      setCampi(await listarCampus());
    } catch {
      setErro("Não foi possível carregar os campi.");
    }
  }

  useEffect(() => {
    // eslint-disable-next-line react-hooks/set-state-in-effect -- busca inicial de dados no mount
    carregar();
  }, []);

  async function aoCriar(evento: FormEvent<HTMLFormElement>) {
    evento.preventDefault();
    setErro(null);
    if (nomeNovo.trim().length === 0) {
      setErro("Informe o nome do campus.");
      return;
    }

    setEnviando(true);
    try {
      await criarCampus({ nome: nomeNovo.trim() });
      setNomeNovo("");
      await carregar();
    } catch (erroCapturado) {
      setErro(erroCapturado instanceof ErroApi ? erroCapturado.message : "Não foi possível criar o campus.");
    } finally {
      setEnviando(false);
    }
  }

  function aoIniciarEdicao(campus: Campus) {
    setEditandoId(campus.id);
    setNomeEdicao(campus.nome);
  }

  async function aoSalvarEdicao(campus: Campus) {
    setErro(null);
    try {
      await atualizarCampus(campus.id, { nome: nomeEdicao.trim() });
      setEditandoId(null);
      await carregar();
    } catch (erroCapturado) {
      setErro(erroCapturado instanceof ErroApi ? erroCapturado.message : "Não foi possível salvar o campus.");
    }
  }

  async function aoDesativar(campus: Campus) {
    if (!confirm(`Desativar o campus "${campus.nome}"? Ele deixa de aparecer nos cadastros e na criação de turmas.`)) {
      return;
    }
    setErro(null);
    try {
      await desativarCampus(campus.id);
      await carregar();
    } catch (erroCapturado) {
      setErro(erroCapturado instanceof ErroApi ? erroCapturado.message : "Não foi possível desativar o campus.");
    }
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="flex items-center gap-2 text-xl font-semibold text-neutral-900">
          <IconeUsuarios className="h-5 w-5 text-neutral-400" />
          Campus
        </h1>
      </div>

      <form onSubmit={aoCriar} noValidate className="flex flex-wrap items-end gap-3 rounded-xl border border-neutral-200 bg-white p-5">
        <div className="w-full max-w-xs">
          <CampoTexto id="novo-campus-nome" label="Novo campus" valor={nomeNovo} onChange={setNomeNovo} placeholder="Nome do campus" />
        </div>
        <button
          type="submit"
          disabled={enviando}
          className="rounded-lg bg-neutral-900 px-4 py-2 text-sm font-medium text-white transition-colors focus:outline-none focus-visible:ring-2 focus-visible:ring-neutral-800 disabled:cursor-not-allowed disabled:bg-neutral-300"
        >
          {enviando ? "Criando..." : "Criar campus"}
        </button>
      </form>

      {erro && (
        <div role="alert" className="rounded-lg border border-red-300 bg-red-50 px-3 py-2 text-sm text-red-700">
          {erro}
        </div>
      )}

      {campi === null && !erro && <p className="text-sm text-neutral-500">Carregando...</p>}

      {campi !== null && campi.length === 0 && <p className="text-sm text-neutral-500">Nenhum campus cadastrado.</p>}

      {campi !== null && campi.length > 0 && (
        <div className="overflow-hidden rounded-xl border border-neutral-200 bg-white">
          <table className="w-full text-left text-sm">
            <thead className="border-b border-neutral-200 bg-neutral-50 text-xs uppercase tracking-wide text-neutral-500">
              <tr>
                <th className="px-4 py-3">Nome</th>
                <th className="px-4 py-3">Status</th>
                <th className="px-4 py-3"></th>
              </tr>
            </thead>
            <tbody>
              {campi.map((campus) => (
                <tr key={campus.id} className="border-b border-neutral-100 last:border-0">
                  <td className="px-4 py-3 font-medium text-neutral-900">
                    {editandoId === campus.id ? (
                      <input
                        value={nomeEdicao}
                        onChange={(evento) => setNomeEdicao(evento.target.value)}
                        className="w-full rounded-lg border border-neutral-300 px-2 py-1 text-sm focus:outline-none focus-visible:ring-2 focus-visible:ring-neutral-800"
                      />
                    ) : (
                      campus.nome
                    )}
                  </td>
                  <td className="px-4 py-3">
                    <span
                      className={`rounded-full px-2 py-0.5 text-xs font-medium ${
                        campus.ativo ? "bg-emerald-100 text-emerald-800" : "bg-neutral-100 text-neutral-600"
                      }`}
                    >
                      {campus.ativo ? "Ativo" : "Inativo"}
                    </span>
                  </td>
                  <td className="px-4 py-3 text-right">
                    {editandoId === campus.id ? (
                      <button
                        type="button"
                        onClick={() => aoSalvarEdicao(campus)}
                        title="Salvar"
                        aria-label={`Salvar campus ${campus.nome}`}
                        className="inline-flex rounded p-1.5 text-emerald-700 transition-colors hover:bg-emerald-50"
                      >
                        <IconeCheckCirculo className="h-4 w-4" />
                      </button>
                    ) : (
                      <div className="flex items-center justify-end gap-1">
                        <button
                          type="button"
                          onClick={() => aoIniciarEdicao(campus)}
                          title="Editar"
                          aria-label={`Editar campus ${campus.nome}`}
                          className="inline-flex rounded p-1.5 text-neutral-500 transition-colors hover:bg-neutral-100 hover:text-neutral-900"
                        >
                          <IconeLapis className="h-4 w-4" />
                        </button>
                        {campus.ativo && (
                          <button
                            type="button"
                            onClick={() => aoDesativar(campus)}
                            title="Desativar"
                            aria-label={`Desativar campus ${campus.nome}`}
                            className="inline-flex rounded p-1.5 text-red-700 transition-colors hover:bg-red-50"
                          >
                            <IconeProibir className="h-4 w-4" />
                          </button>
                        )}
                      </div>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
