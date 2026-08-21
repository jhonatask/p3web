package br.com.familychurch.p3.presenca.exception;

import br.com.familychurch.p3.common.exception.ForbiddenException;

public class ValidadorNaoIdentificadoException extends ForbiddenException {

	public ValidadorNaoIdentificadoException() {
		super("Sua conta nao esta vinculada a um responsavel cadastrado - "
				+ "nao e possivel registrar presenca manual com esse usuario.");
	}

}
