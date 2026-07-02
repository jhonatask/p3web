package br.com.familychurch.p3.aula.dto;

import br.com.familychurch.p3.aula.Aula;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record AulaResponse(

		UUID id,
		UUID turmaId,
		Integer numero,
		String modulo,
		LocalDate data,
		LocalDateTime janelaAbertura,
		LocalDateTime janelaFechamento,
		String qrToken,
		boolean janelaAberta

) {

	public static AulaResponse from(Aula aula) {
		return new AulaResponse(
				aula.getId(),
				aula.getTurma().getId(),
				aula.getNumero(),
				aula.getModulo(),
				aula.getData(),
				aula.getJanelaAbertura(),
				aula.getJanelaFechamento(),
				aula.getQrToken(),
				aula.janelaAberta(LocalDateTime.now()));
	}

}
