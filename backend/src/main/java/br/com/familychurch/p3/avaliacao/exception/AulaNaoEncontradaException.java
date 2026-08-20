package br.com.familychurch.p3.avaliacao.exception;

import br.com.familychurch.p3.common.exception.NotFoundException;
import java.util.UUID;

public class AulaNaoEncontradaException extends NotFoundException {

	public AulaNaoEncontradaException(UUID id) {
		super("Esta aula nao foi encontrada.");
	}

}
