package br.com.familychurch.p3.aluno.exception;

import br.com.familychurch.p3.common.exception.ConflictException;

public class AlunoJaCadastradoException extends ConflictException {

	public AlunoJaCadastradoException(String cpf) {
		super("Ja existe um aluno cadastrado com o CPF '%s'".formatted(cpf));
	}

}
