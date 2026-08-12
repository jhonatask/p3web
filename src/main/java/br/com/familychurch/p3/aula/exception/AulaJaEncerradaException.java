package br.com.familychurch.p3.aula.exception;

import br.com.familychurch.p3.common.exception.ConflictException;
import java.util.UUID;

public class AulaJaEncerradaException extends ConflictException {

	public AulaJaEncerradaException(UUID tipoAulaId) {
		super("A aula do tipo '%s' ja foi encerrada e nao aceita mais alteracao de data".formatted(tipoAulaId));
	}

}
