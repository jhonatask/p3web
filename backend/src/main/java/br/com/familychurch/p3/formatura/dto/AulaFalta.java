package br.com.familychurch.p3.formatura.dto;

import java.util.UUID;

public record AulaFalta(

		UUID tipoAulaId,
		Integer numero,
		String tema

) {
}
