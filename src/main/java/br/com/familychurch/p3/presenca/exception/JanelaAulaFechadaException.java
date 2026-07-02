package br.com.familychurch.p3.presenca.exception;

import br.com.familychurch.p3.common.exception.ValidationException;
import java.util.UUID;

public class JanelaAulaFechadaException extends ValidationException {

	public JanelaAulaFechadaException(UUID aulaId) {
		super("A janela de check-in da aula '%s' esta fechada no momento".formatted(aulaId));
	}

}
