package br.com.familychurch.p3.curso.exception;

import br.com.familychurch.p3.common.exception.ConflictException;

public class CursoJaExisteException extends ConflictException {

	public CursoJaExisteException(String nome) {
		super("Ja existe um curso ativo com o nome '%s'".formatted(nome));
	}

}
