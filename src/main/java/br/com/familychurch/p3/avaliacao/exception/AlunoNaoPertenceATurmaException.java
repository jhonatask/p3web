package br.com.familychurch.p3.avaliacao.exception;

import br.com.familychurch.p3.common.exception.ValidationException;
import java.util.UUID;

public class AlunoNaoPertenceATurmaException extends ValidationException {

	public AlunoNaoPertenceATurmaException(UUID alunoId, UUID aulaId) {
		super("Aluno '%s' nao pertence a turma da aula '%s'".formatted(alunoId, aulaId));
	}

}
