package br.com.familychurch.p3.participante.exception;

import br.com.familychurch.p3.common.exception.ValidationException;

public class QuantidadeFilhosObrigatoriaException extends ValidationException {

	public QuantidadeFilhosObrigatoriaException() {
		super("Informe a quantidade de filhos quando 'tem filhos' for verdadeiro");
	}

}
