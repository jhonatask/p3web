package br.com.familychurch.p3.common.exception;

public abstract class ValidationException extends RuntimeException {

	protected ValidationException(String mensagem) {
		super(mensagem);
	}

}
