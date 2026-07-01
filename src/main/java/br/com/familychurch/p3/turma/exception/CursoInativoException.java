package br.com.familychurch.p3.turma.exception;

import java.util.UUID;

public class CursoInativoException extends RuntimeException {

	public CursoInativoException(UUID cursoId) {
		super("Curso '%s' esta inativo e nao pode receber novas turmas".formatted(cursoId));
	}

}
