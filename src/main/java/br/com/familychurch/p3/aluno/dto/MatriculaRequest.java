package br.com.familychurch.p3.aluno.dto;

import br.com.familychurch.p3.common.validation.Cpf;
import jakarta.validation.constraints.NotBlank;

public record MatriculaRequest(

		@Cpf
		String cpf,

		@NotBlank
		String token

) {
}
