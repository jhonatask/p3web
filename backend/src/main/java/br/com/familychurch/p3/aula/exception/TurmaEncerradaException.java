package br.com.familychurch.p3.aula.exception;

import br.com.familychurch.p3.common.exception.ConflictException;
import java.util.UUID;

public class TurmaEncerradaException extends ConflictException {

	public TurmaEncerradaException(UUID turmaId) {
		super("Turma '%s' esta encerrada e nao aceita novas aulas".formatted(turmaId));
	}

	public TurmaEncerradaException(String tokenCadastro) {
		super("Turma referente ao token '%s' esta encerrada".formatted(tokenCadastro));
	}

}
