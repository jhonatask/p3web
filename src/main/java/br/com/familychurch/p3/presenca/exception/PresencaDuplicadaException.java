package br.com.familychurch.p3.presenca.exception;

import br.com.familychurch.p3.common.exception.ConflictException;
import java.util.UUID;

public class PresencaDuplicadaException extends ConflictException {

	public PresencaDuplicadaException(UUID alunoId, UUID aulaId) {
		super("Aluno '%s' ja possui presenca registrada na aula '%s'".formatted(alunoId, aulaId));
	}

}
