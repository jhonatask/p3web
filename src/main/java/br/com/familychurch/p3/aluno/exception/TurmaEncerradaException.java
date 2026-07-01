package br.com.familychurch.p3.aluno.exception;

public class TurmaEncerradaException extends RuntimeException {

	public TurmaEncerradaException(String tokenCadastro) {
		super("Turma referente ao token '%s' esta encerrada e nao aceita novos cadastros".formatted(tokenCadastro));
	}

}
