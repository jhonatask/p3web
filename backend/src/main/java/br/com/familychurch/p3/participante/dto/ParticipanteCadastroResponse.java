package br.com.familychurch.p3.participante.dto;

import br.com.familychurch.p3.participante.Participante;
import java.util.UUID;

public record ParticipanteCadastroResponse(

		UUID id,
		String nome,
		String cpf

) {

	public static ParticipanteCadastroResponse from(Participante participante) {
		return new ParticipanteCadastroResponse(participante.getId(), participante.getNome(), participante.getCpf());
	}

}
