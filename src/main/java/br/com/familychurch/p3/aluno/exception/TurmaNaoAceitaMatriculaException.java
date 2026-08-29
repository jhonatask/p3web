package br.com.familychurch.p3.aluno.exception;

import br.com.familychurch.p3.common.exception.ConflictException;
import java.util.UUID;

public class TurmaNaoAceitaMatriculaException extends ConflictException {

	public TurmaNaoAceitaMatriculaException(UUID turmaId) {
		super("Turma '%s' nao esta aberta para matricula direta - ja comecou ou esta encerrada".formatted(turmaId));
	}

}
