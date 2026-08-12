package br.com.familychurch.p3.avaliacao.exception;

import br.com.familychurch.p3.common.exception.NotFoundException;
import java.util.UUID;

public class CheckinNaoRealizadoException extends NotFoundException {

	public CheckinNaoRealizadoException(UUID participanteId, UUID aulaId) {
		super("Voce ainda nao registrou presenca nesta aula - volte e informe seu CPF na chamada antes de avaliar.");
	}

}
