package br.com.familychurch.p3.interesse.exception;

import br.com.familychurch.p3.common.exception.NotFoundException;
import java.util.UUID;

public class CheckinNaoRealizadoException extends NotFoundException {

	public CheckinNaoRealizadoException(UUID alunoId, UUID aulaId) {
		super("Aluno '%s' ainda nao fez check-in na aula '%s', interesse nao pode ser registrado"
				.formatted(alunoId, aulaId));
	}

}
