package br.com.familychurch.p3.aluno.exception;

import br.com.familychurch.p3.common.exception.ConflictException;

public class TurmaEncerradaException extends ConflictException {

	public TurmaEncerradaException(String tokenCadastro) {
		super("Turma referente ao token '%s' esta encerrada e nao aceita novos cadastros".formatted(tokenCadastro));
	}

}
