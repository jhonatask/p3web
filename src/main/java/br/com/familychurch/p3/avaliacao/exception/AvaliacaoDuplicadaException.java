package br.com.familychurch.p3.avaliacao.exception;

import br.com.familychurch.p3.common.exception.ConflictException;
import java.util.UUID;

public class AvaliacaoDuplicadaException extends ConflictException {

	public AvaliacaoDuplicadaException(UUID alunoId, UUID aulaId) {
		super("Aluno '%s' ja avaliou a aula '%s'".formatted(alunoId, aulaId));
	}

}
