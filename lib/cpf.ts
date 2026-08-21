import { somenteDigitos } from "./texto";

function calcularDigitoVerificador(base: string, pesoInicial: number): number {
  const soma = base
    .split("")
    .reduce((acumulado, digito, indice) => acumulado + Number(digito) * (pesoInicial - indice), 0);
  const resto = (soma * 10) % 11;
  return resto === 10 ? 0 : resto;
}

export function validarCpf(valor: string): boolean {
  const cpf = somenteDigitos(valor);

  if (cpf.length !== 11) {
    return false;
  }

  if (/^(\d)\1{10}$/.test(cpf)) {
    return false;
  }

  const primeiroDigito = calcularDigitoVerificador(cpf.slice(0, 9), 10);
  if (primeiroDigito !== Number(cpf[9])) {
    return false;
  }

  const segundoDigito = calcularDigitoVerificador(cpf.slice(0, 10), 11);
  if (segundoDigito !== Number(cpf[10])) {
    return false;
  }

  return true;
}
