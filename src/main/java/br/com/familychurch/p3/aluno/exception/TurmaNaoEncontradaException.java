package br.com.familychurch.p3.aluno.exception;

import br.com.familychurch.p3.common.exception.NotFoundException;

public class TurmaNaoEncontradaException extends NotFoundException {

	public TurmaNaoEncontradaException(String tokenCadastro) {
		super("Turma nao encontrada para o token de cadastro '%s'".formatted(tokenCadastro));
	}

}
