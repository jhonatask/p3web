package br.com.familychurch.p3.formatura.dto;

import java.util.UUID;

public record UltimaFaltaResponse(

		UUID participanteId,
		String nome

) {
}
