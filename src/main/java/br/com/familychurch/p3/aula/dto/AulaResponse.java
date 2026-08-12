package br.com.familychurch.p3.aula.dto;

import br.com.familychurch.p3.aula.Aula;
import br.com.familychurch.p3.aula.ModuloAula;
import br.com.familychurch.p3.aula.StatusAula;
import java.time.LocalDate;
import java.util.UUID;

public record AulaResponse(

		UUID id,
		UUID turmaId,
		UUID tipoAulaId,
		ModuloAula modulo,
		Integer numero,
		String tema,
		LocalDate data,
		StatusAula status

) {

	public static AulaResponse from(Aula aula) {
		return new AulaResponse(
				aula.getId(),
				aula.getTurma().getId(),
				aula.getTipoAula().getId(),
				aula.getTipoAula().getModulo(),
				aula.getTipoAula().getNumero(),
				aula.getTipoAula().getTema(),
				aula.getData(),
				aula.getStatus());
	}

}
