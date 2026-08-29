package br.com.familychurch.p3.aluno.dto;

import java.util.UUID;

public record MatriculaResponse(

		UUID turmaId,
		String turmaNome,
		boolean jaEstavaMatriculado

) {
}
