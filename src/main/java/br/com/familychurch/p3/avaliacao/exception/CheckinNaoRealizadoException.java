package br.com.familychurch.p3.avaliacao.exception;

import br.com.familychurch.p3.common.exception.NotFoundException;
import java.util.UUID;

public class CheckinNaoRealizadoException extends NotFoundException {

	public CheckinNaoRealizadoException(UUID participanteId, UUID aulaId) {
		super("Participante '%s' ainda nao registrou presenca na aula '%s', avaliacao nao pode ser registrada"
				.formatted(participanteId, aulaId));
	}

}
