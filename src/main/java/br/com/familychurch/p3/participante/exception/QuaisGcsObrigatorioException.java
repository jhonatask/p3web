package br.com.familychurch.p3.participante.exception;

import br.com.familychurch.p3.common.exception.ValidationException;

public class QuaisGcsObrigatorioException extends ValidationException {

	public QuaisGcsObrigatorioException() {
		super("Informe pelo menos um GC quando 'participa de GC' for verdadeiro");
	}

}
