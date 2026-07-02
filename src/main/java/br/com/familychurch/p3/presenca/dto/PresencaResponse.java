package br.com.familychurch.p3.presenca.dto;

import br.com.familychurch.p3.presenca.MetodoPresenca;
import br.com.familychurch.p3.presenca.Presenca;
import java.time.LocalDateTime;
import java.util.UUID;

public record PresencaResponse(

		UUID id,
		UUID alunoId,
		String alunoNome,
		UUID aulaId,
		LocalDateTime timestampCheckin,
		MetodoPresenca metodo,
		UUID validadoPorId,
		String validadoPorNome

) {

	public static PresencaResponse from(Presenca presenca) {
		return new PresencaResponse(
				presenca.getId(),
				presenca.getAluno().getId(),
				presenca.getAluno().getNome(),
				presenca.getAula().getId(),
				presenca.getTimestampCheckin(),
				presenca.getMetodo(),
				presenca.getValidadoPor() != null ? presenca.getValidadoPor().getId() : null,
				presenca.getValidadoPor() != null ? presenca.getValidadoPor().getNome() : null);
	}

}
