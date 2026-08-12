package br.com.familychurch.p3.avaliacao.dto;

import br.com.familychurch.p3.avaliacao.Avaliacao;
import br.com.familychurch.p3.avaliacao.DidaticaAvaliacao;
import java.util.UUID;

public record AvaliacaoResponse(

		UUID id,
		UUID participanteId,
		String participanteNome,
		UUID aulaId,
		Integer nota,
		DidaticaAvaliacao didatica,
		String sugestao,
		Integer nps,
		String desejaServir,
		String habilidades

) {

	public static AvaliacaoResponse from(Avaliacao avaliacao) {
		return new AvaliacaoResponse(
				avaliacao.getId(),
				avaliacao.getParticipante().getId(),
				avaliacao.getParticipante().getNome(),
				avaliacao.getAula().getId(),
				avaliacao.getNota(),
				avaliacao.getDidatica(),
				avaliacao.getSugestao(),
				avaliacao.getNps(),
				avaliacao.getDesejaServir(),
				avaliacao.getHabilidades());
	}

}
