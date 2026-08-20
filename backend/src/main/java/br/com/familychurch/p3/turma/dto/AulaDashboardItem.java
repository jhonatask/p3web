package br.com.familychurch.p3.turma.dto;

import java.time.LocalDate;
import java.util.UUID;

public record AulaDashboardItem(

		UUID aulaId,
		Integer numero,
		LocalDate data,
		long totalPresencas,
		double percentualPresenca,
		Double notaMedia,
		Double npsMedia

) {
}
