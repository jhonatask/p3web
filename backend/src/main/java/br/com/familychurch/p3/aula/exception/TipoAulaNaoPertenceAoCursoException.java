package br.com.familychurch.p3.aula.exception;

import br.com.familychurch.p3.common.exception.ValidationException;
import java.util.UUID;

public class TipoAulaNaoPertenceAoCursoException extends ValidationException {

	public TipoAulaNaoPertenceAoCursoException(UUID tipoAulaId, UUID cursoId) {
		super("Tipo de aula '%s' nao pertence ao curso '%s' da turma".formatted(tipoAulaId, cursoId));
	}

}
