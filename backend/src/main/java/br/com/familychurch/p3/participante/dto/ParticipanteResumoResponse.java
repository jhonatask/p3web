package br.com.familychurch.p3.participante.dto;

import br.com.familychurch.p3.participante.Participante;
import java.util.UUID;

/**
 * Resposta minima usada pela chamada publica: so confirma o nome na tela
 * ("Ola, Fulano") e segue direto para a avaliacao - nunca expoe o cadastro
 * completo por esse caminho.
 */
public record ParticipanteResumoResponse(

		UUID id,
		String nome

) {

	public static ParticipanteResumoResponse from(Participante participante) {
		return new ParticipanteResumoResponse(participante.getId(), participante.getNome());
	}

}
