import { somenteDigitos } from "./texto";

export type EnderecoViaCep = {
  logradouro: string;
  bairro: string;
  localidade: string;
  uf: string;
  erro?: boolean;
};

export async function buscarEnderecoPorCep(cep: string): Promise<EnderecoViaCep | null> {
  const digitos = somenteDigitos(cep);
  if (digitos.length !== 8) {
    return null;
  }

  try {
    const resposta = await fetch(`https://viacep.com.br/ws/${digitos}/json/`);
    if (!resposta.ok) {
      return null;
    }
    const dados = (await resposta.json()) as EnderecoViaCep;
    if (dados.erro) {
      return null;
    }
    return dados;
  } catch {
    return null;
  }
}
