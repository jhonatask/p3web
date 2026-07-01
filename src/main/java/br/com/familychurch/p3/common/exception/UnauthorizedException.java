package br.com.familychurch.p3.common.exception;

public abstract class UnauthorizedException extends RuntimeException {

	protected UnauthorizedException(String mensagem) {
		super(mensagem);
	}

}
