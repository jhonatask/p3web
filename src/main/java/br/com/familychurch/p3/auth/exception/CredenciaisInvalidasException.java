package br.com.familychurch.p3.auth.exception;

import br.com.familychurch.p3.common.exception.UnauthorizedException;

public class CredenciaisInvalidasException extends UnauthorizedException {

	public CredenciaisInvalidasException() {
		super("Email ou senha invalidos");
	}

}
