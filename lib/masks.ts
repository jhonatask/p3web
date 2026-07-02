import { somenteDigitos } from "./texto";

export function mascararCpf(valor: string): string {
  const digitos = somenteDigitos(valor).slice(0, 11);
  const partes = [digitos.slice(0, 3), digitos.slice(3, 6), digitos.slice(6, 9), digitos.slice(9, 11)];

  let resultado = partes[0];
  if (partes[1]) resultado += `.${partes[1]}`;
  if (partes[2]) resultado += `.${partes[2]}`;
  if (partes[3]) resultado += `-${partes[3]}`;
  return resultado;
}

export function mascararCelular(valor: string): string {
  const digitos = somenteDigitos(valor).slice(0, 11);

  if (digitos.length === 0) {
    return "";
  }

  const ddd = digitos.slice(0, 2);
  if (digitos.length <= 2) {
    return `(${ddd}`;
  }

  const numero = digitos.slice(2);
  if (numero.length <= 4) {
    return `(${ddd}) ${numero}`;
  }

  const prefixo = numero.slice(0, -4);
  const sufixo = numero.slice(-4);
  return `(${ddd}) ${prefixo}-${sufixo}`;
}
