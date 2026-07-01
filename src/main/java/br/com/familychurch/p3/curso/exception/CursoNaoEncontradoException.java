package br.com.familychurch.p3.curso.exception;

import java.util.UUID;

public class CursoNaoEncontradoException extends RuntimeException {

	public CursoNaoEncontradoException(UUID id) {
		super("Curso nao encontrado para o id '%s'".formatted(id));
	}

}
