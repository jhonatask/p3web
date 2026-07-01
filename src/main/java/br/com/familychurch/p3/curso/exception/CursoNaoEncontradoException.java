package br.com.familychurch.p3.curso.exception;

import br.com.familychurch.p3.common.exception.NotFoundException;
import java.util.UUID;

public class CursoNaoEncontradoException extends NotFoundException {

	public CursoNaoEncontradoException(UUID id) {
		super("Curso nao encontrado para o id '%s'".formatted(id));
	}

}
