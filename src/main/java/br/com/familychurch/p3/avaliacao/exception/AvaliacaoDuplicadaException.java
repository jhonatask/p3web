package br.com.familychurch.p3.avaliacao.exception;

import br.com.familychurch.p3.common.exception.ConflictException;
import java.util.UUID;

public class AvaliacaoDuplicadaException extends ConflictException {

	public AvaliacaoDuplicadaException(UUID participanteId, UUID aulaId) {
		super("Voce ja enviou uma avaliacao para esta aula.");
	}

}
