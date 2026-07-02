package br.com.familychurch.p3.turma.exception;

import br.com.familychurch.p3.common.exception.ConflictException;

public class TurmaEncerradaException extends ConflictException {

	public TurmaEncerradaException(String tokenCadastro) {
		super("Turma referente ao token '%s' esta encerrada".formatted(tokenCadastro));
	}

}
