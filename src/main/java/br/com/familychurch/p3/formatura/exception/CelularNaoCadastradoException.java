package br.com.familychurch.p3.formatura.exception;

import br.com.familychurch.p3.common.exception.ValidationException;
import java.util.UUID;

public class CelularNaoCadastradoException extends ValidationException {

	public CelularNaoCadastradoException(UUID participanteId) {
		super("Participante '%s' nao tem celular cadastrado, nao e possivel gerar link do WhatsApp"
				.formatted(participanteId));
	}

}
