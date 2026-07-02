package br.com.familychurch.p3.aula.exception;

import br.com.familychurch.p3.common.exception.ValidationException;

public class JanelaInvalidaException extends ValidationException {

	public JanelaInvalidaException() {
		super("A janela de fechamento da aula deve ser posterior a janela de abertura");
	}

}
