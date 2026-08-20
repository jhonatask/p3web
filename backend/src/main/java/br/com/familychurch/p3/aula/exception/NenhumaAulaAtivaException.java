package br.com.familychurch.p3.aula.exception;

import br.com.familychurch.p3.common.exception.NotFoundException;
import java.util.UUID;

public class NenhumaAulaAtivaException extends NotFoundException {

	public NenhumaAulaAtivaException(UUID turmaId) {
		super("Nenhuma aula esta ativa nesta turma no momento.");
	}

}
