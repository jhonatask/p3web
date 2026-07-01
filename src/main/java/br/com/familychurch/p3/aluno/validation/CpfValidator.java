package br.com.familychurch.p3.aluno.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CpfValidator implements ConstraintValidator<Cpf, String> {

	@Override
	public boolean isValid(String cpf, ConstraintValidatorContext context) {
		if (cpf == null) {
			return false;
		}
		String digitos = cpf.replaceAll("\\D", "");
		if (digitos.length() != 11 || todosDigitosIguais(digitos)) {
			return false;
		}
		int primeiroDigitoVerificador = calcularDigitoVerificador(digitos, 9);
		int segundoDigitoVerificador = calcularDigitoVerificador(digitos, 10);
		return primeiroDigitoVerificador == Character.getNumericValue(digitos.charAt(9))
				&& segundoDigitoVerificador == Character.getNumericValue(digitos.charAt(10));
	}

	private boolean todosDigitosIguais(String digitos) {
		return digitos.chars().distinct().count() == 1;
	}

	private int calcularDigitoVerificador(String digitos, int quantidadeDigitosBase) {
		int peso = quantidadeDigitosBase + 1;
		int soma = 0;
		for (int i = 0; i < quantidadeDigitosBase; i++) {
			soma += Character.getNumericValue(digitos.charAt(i)) * peso;
			peso--;
		}
		int resto = (soma * 10) % 11;
		return resto == 10 ? 0 : resto;
	}

}
