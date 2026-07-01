package br.com.familychurch.p3.turma.exception;

import java.util.UUID;

public class TurmaNaoEncontradaException extends RuntimeException {

	public TurmaNaoEncontradaException(UUID id) {
		super("Turma nao encontrada para o id '%s'".formatted(id));
	}

}
