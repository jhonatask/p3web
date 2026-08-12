package br.com.familychurch.p3.formatura.dto;

import java.util.List;
import java.util.UUID;

public record StatusFormatura(

		UUID participanteId,
		List<StatusModulo> modulos,
		boolean apto,
		List<AulaFalta> aRepor

) {
}
