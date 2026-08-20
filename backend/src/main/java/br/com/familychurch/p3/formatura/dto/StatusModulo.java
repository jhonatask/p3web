package br.com.familychurch.p3.formatura.dto;

import br.com.familychurch.p3.aula.ModuloAula;
import java.util.List;

public record StatusModulo(

		ModuloAula modulo,
		int totalAulas,
		List<AulaFalta> faltas,
		boolean reprovado

) {
}
