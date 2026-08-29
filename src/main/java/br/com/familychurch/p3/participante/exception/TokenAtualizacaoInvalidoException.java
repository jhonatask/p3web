package br.com.familychurch.p3.participante.exception;

import br.com.familychurch.p3.common.exception.ForbiddenException;

public class TokenAtualizacaoInvalidoException extends ForbiddenException {

	public TokenAtualizacaoInvalidoException() {
		super("Token de atualizacao de cadastro invalido ou expirado.");
	}

}
