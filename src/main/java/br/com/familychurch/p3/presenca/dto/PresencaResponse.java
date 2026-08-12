package br.com.familychurch.p3.presenca.dto;

import br.com.familychurch.p3.presenca.MetodoPresenca;
import br.com.familychurch.p3.presenca.OrigemPresenca;
import br.com.familychurch.p3.presenca.Presenca;
import java.time.LocalDateTime;
import java.util.UUID;

public record PresencaResponse(

		UUID id,
		UUID participanteId,
		String participanteNome,
		UUID turmaId,
		UUID aulaId,
		LocalDateTime timestampCheckin,
		MetodoPresenca metodo,
		OrigemPresenca origem,
		/**
		 * Nome da turma de origem (matricula) do participante - permite o
		 * front avisar "isso conta como reposicao, sua turma e X" quando
		 * origem=REPOSICAO. Nulo quando nao computado (ex.: listagem por
		 * aula, que nao precisa desse contexto).
		 */
		String turmaOrigemNome,
		UUID validadoPorId,
		String validadoPorNome

) {

	public static PresencaResponse from(Presenca presenca) {
		return from(presenca, null);
	}

	public static PresencaResponse from(Presenca presenca, String turmaOrigemNome) {
		return new PresencaResponse(
				presenca.getId(),
				presenca.getParticipante().getId(),
				presenca.getParticipante().getNome(),
				presenca.getTurma().getId(),
				presenca.getAula().getId(),
				presenca.getTimestampCheckin(),
				presenca.getMetodo(),
				presenca.getOrigem(),
				turmaOrigemNome,
				presenca.getValidadoPor() != null ? presenca.getValidadoPor().getId() : null,
				presenca.getValidadoPor() != null ? presenca.getValidadoPor().getNome() : null);
	}

}
