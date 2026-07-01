package br.com.familychurch.p3.turma.exception;

import br.com.familychurch.p3.common.exception.NotFoundException;
import java.util.UUID;

public class TurmaNaoEncontradaException extends NotFoundException {

	public TurmaNaoEncontradaException(UUID id) {
		super("Turma nao encontrada para o id '%s'".formatted(id));
	}

}
