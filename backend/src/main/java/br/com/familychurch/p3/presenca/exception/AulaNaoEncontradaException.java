package br.com.familychurch.p3.presenca.exception;

import br.com.familychurch.p3.common.exception.NotFoundException;
import java.util.UUID;

public class AulaNaoEncontradaException extends NotFoundException {

	public AulaNaoEncontradaException(UUID id) {
		super("Aula nao encontrada para o id '%s'".formatted(id));
	}

}
