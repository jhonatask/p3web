package br.com.familychurch.p3.auth.exception;

public class CredenciaisInvalidasException extends RuntimeException {

	public CredenciaisInvalidasException() {
		super("Email ou senha invalidos");
	}

}
