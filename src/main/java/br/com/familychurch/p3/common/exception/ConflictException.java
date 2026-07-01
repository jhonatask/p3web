package br.com.familychurch.p3.common.exception;

public abstract class ConflictException extends RuntimeException {

	protected ConflictException(String mensagem) {
		super(mensagem);
	}

}
