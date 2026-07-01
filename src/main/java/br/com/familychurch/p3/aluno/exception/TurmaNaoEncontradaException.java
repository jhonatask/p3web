package br.com.familychurch.p3.aluno.exception;

public class TurmaNaoEncontradaException extends RuntimeException {

	public TurmaNaoEncontradaException(String tokenCadastro) {
		super("Turma nao encontrada para o token de cadastro '%s'".formatted(tokenCadastro));
	}

}
