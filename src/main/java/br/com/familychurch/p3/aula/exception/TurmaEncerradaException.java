package br.com.familychurch.p3.aula.exception;

import br.com.familychurch.p3.common.exception.ConflictException;
import java.util.UUID;

public class TurmaEncerradaException extends ConflictException {

	public TurmaEncerradaException(UUID turmaId) {
		super("Turma '%s' esta encerrada e nao aceita novas aulas".formatted(turmaId));
	}

}
