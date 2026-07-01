package br.com.familychurch.p3.common.exception;

public abstract class NotFoundException extends RuntimeException {

	protected NotFoundException(String mensagem) {
		super(mensagem);
	}

}
