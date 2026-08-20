package br.com.familychurch.p3.formatura.exception;

import br.com.familychurch.p3.common.exception.NotFoundException;
import java.util.UUID;

public class MatriculaNaoEncontradaException extends NotFoundException {

	public MatriculaNaoEncontradaException(UUID participanteId) {
		super("Participante '%s' ainda nao tem matricula na Escola de Hombridade".formatted(participanteId));
	}

}
