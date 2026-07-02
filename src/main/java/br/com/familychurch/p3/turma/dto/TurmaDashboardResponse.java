package br.com.familychurch.p3.turma.dto;

import java.util.List;
import java.util.UUID;

public record TurmaDashboardResponse(

		UUID turmaId,
		String turmaNome,
		int totalAulas,
		int totalAlunos,
		double frequenciaMedia,
		Double notaMedia,
		Double npsMedia,
		List<AulaDashboardItem> porAula

) {
}
