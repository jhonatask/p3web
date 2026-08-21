package br.com.familychurch.p3.common.exception;

public abstract class ForbiddenException extends RuntimeException {

	protected ForbiddenException(String mensagem) {
		super(mensagem);
	}

}
