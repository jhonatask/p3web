package br.com.familychurch.p3.formatura.dto;

import java.util.UUID;

public record PainelLinhaResponse(

		UUID participanteId,
		String nome,
		StatusFormatura formatura

) {
}
