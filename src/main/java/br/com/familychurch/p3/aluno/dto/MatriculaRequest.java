package br.com.familychurch.p3.aluno.dto;

import br.com.familychurch.p3.common.validation.Cpf;

public record MatriculaRequest(

		@Cpf
		String cpf

) {
}
