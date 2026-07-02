package br.com.familychurch.p3.interesse.exception;

import br.com.familychurch.p3.common.exception.ConflictException;
import java.util.UUID;

public class InteresseDuplicadoException extends ConflictException {

	public InteresseDuplicadoException(UUID alunoId, UUID aulaId) {
		super("Aluno '%s' ja registrou interesse na aula '%s'".formatted(alunoId, aulaId));
	}

}
