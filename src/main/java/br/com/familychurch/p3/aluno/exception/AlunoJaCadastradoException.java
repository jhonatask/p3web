package br.com.familychurch.p3.aluno.exception;

public class AlunoJaCadastradoException extends RuntimeException {

	public AlunoJaCadastradoException(String cpf) {
		super("Ja existe um aluno cadastrado com o CPF '%s'".formatted(cpf));
	}

}
