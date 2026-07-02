package br.com.familychurch.p3.avaliacao.dto;

import br.com.familychurch.p3.avaliacao.Avaliacao;
import br.com.familychurch.p3.avaliacao.DidaticaAvaliacao;
import java.util.UUID;

public record AvaliacaoResponse(

		UUID id,
		UUID alunoId,
		String alunoNome,
		UUID aulaId,
		Integer nota,
		DidaticaAvaliacao didatica,
		String sugestao,
		Integer nps

) {

	public static AvaliacaoResponse from(Avaliacao avaliacao) {
		return new AvaliacaoResponse(
				avaliacao.getId(),
				avaliacao.getAluno().getId(),
				avaliacao.getAluno().getNome(),
				avaliacao.getAula().getId(),
				avaliacao.getNota(),
				avaliacao.getDidatica(),
				avaliacao.getSugestao(),
				avaliacao.getNps());
	}

}
