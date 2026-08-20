package br.com.familychurch.p3.turma.exception;

import br.com.familychurch.p3.common.exception.ConflictException;
import java.util.UUID;

public class CursoInativoException extends ConflictException {

	public CursoInativoException(UUID cursoId) {
		super("Curso '%s' esta inativo e nao pode receber novas turmas".formatted(cursoId));
	}

}
