"use client";

import { useState } from "react";
import type { TurmaPublica } from "@/lib/api";
import CabecalhoTurma from "./CabecalhoTurma";
import FormularioCadastro from "./FormularioCadastro";
import CadastroConcluido from "./CadastroConcluido";
import CartaoPagina from "./CartaoPagina";

type CadastroClienteProps = {
  token: string;
  turma: TurmaPublica;
};

export default function CadastroCliente({ token, turma }: CadastroClienteProps) {
  const [nomeCadastrado, setNomeCadastrado] = useState<string | null>(null);

  return (
    <CartaoPagina>
      {nomeCadastrado ? (
        <CadastroConcluido nome={nomeCadastrado} />
      ) : (
        <>
          <CabecalhoTurma turma={turma} />
          <FormularioCadastro token={token} onCadastroConcluido={setNomeCadastrado} />
        </>
      )}
    </CartaoPagina>
  );
}
