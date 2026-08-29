package br.com.familychurch.p3.aluno.exception;

import br.com.familychurch.p3.common.exception.ForbiddenException;

public class TokenMatriculaInvalidoException extends ForbiddenException {

	public TokenMatriculaInvalidoException() {
		super("Token de matricula invalido ou expirado.");
	}

}
