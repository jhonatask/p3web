package br.com.familychurch.p3.presenca.exception;

import br.com.familychurch.p3.common.exception.NotFoundException;

public class AlunoNaoEncontradoException extends NotFoundException {

	public AlunoNaoEncontradoException(String cpf) {
		super("Nenhum aluno cadastrado com o CPF '%s'".formatted(cpf));
	}

}
