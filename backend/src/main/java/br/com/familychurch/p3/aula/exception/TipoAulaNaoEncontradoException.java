package br.com.familychurch.p3.aula.exception;

import br.com.familychurch.p3.common.exception.NotFoundException;
import java.util.UUID;

public class TipoAulaNaoEncontradoException extends NotFoundException {

	public TipoAulaNaoEncontradoException(UUID id) {
		super("Tipo de aula nao encontrado para o id '%s'".formatted(id));
	}

}
