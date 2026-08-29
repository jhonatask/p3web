package br.com.familychurch.p3.formatura.dto;

import java.util.List;
import java.util.UUID;

public record PainelTurmaResponse(

		UUID turmaId,
		String turmaNome,
		int totalMatriculados,
		AulaFalta aulaReferenciaUltimasFaltas,
		List<UltimaFaltaResponse> ultimasFaltas,
		List<PainelLinhaResponse> linhas

) {
}
