package br.com.familychurch.p3.formatura.exception;

import br.com.familychurch.p3.common.exception.ValidationException;
import java.util.UUID;

public class NenhumaReposicaoPendenteException extends ValidationException {

	public NenhumaReposicaoPendenteException(UUID participanteId) {
		super("Participante '%s' nao tem nenhuma aula pendente de reposicao".formatted(participanteId));
	}

}
