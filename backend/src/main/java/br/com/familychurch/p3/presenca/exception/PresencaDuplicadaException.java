package br.com.familychurch.p3.presenca.exception;

import br.com.familychurch.p3.common.exception.ConflictException;
import java.util.UUID;

public class PresencaDuplicadaException extends ConflictException {

	public PresencaDuplicadaException(UUID participanteId, UUID aulaId) {
		super("Este participante ja possui presenca registrada nesta aula.");
	}

}
