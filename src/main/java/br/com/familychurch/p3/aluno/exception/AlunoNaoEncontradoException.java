package br.com.familychurch.p3.aluno.exception;

import br.com.familychurch.p3.common.exception.NotFoundException;
import java.util.UUID;

public class AlunoNaoEncontradoException extends NotFoundException {

	public AlunoNaoEncontradoException(UUID turmaId, UUID alunoId) {
		super("Aluno '%s' nao encontrado na turma '%s'".formatted(alunoId, turmaId));
	}

}
