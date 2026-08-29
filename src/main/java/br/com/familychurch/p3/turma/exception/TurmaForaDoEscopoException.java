package br.com.familychurch.p3.turma.exception;

import br.com.familychurch.p3.common.exception.ForbiddenException;

public class TurmaForaDoEscopoException extends ForbiddenException {

	public TurmaForaDoEscopoException() {
		super("Voce nao tem acesso a esta turma.");
	}

}
