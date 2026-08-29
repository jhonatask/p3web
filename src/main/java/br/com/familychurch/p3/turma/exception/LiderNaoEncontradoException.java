package br.com.familychurch.p3.turma.exception;

import br.com.familychurch.p3.common.exception.NotFoundException;
import java.util.UUID;

public class LiderNaoEncontradoException extends NotFoundException {

	public LiderNaoEncontradoException(UUID id) {
		super("Lider nao encontrado para o id '%s'".formatted(id));
	}

}
