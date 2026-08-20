package br.com.familychurch.p3.participante.exception;

import br.com.familychurch.p3.common.exception.NotFoundException;

public class ParticipanteNaoEncontradoException extends NotFoundException {

	public ParticipanteNaoEncontradoException(String cpf) {
		super("Nenhum participante cadastrado com o CPF '%s'".formatted(cpf));
	}

}
