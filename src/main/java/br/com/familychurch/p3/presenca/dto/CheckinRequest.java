package br.com.familychurch.p3.presenca.dto;

import br.com.familychurch.p3.aluno.validation.Cpf;
import jakarta.validation.constraints.NotBlank;

public record CheckinRequest(

		@NotBlank
		String qrToken,

		@Cpf
		String cpf

) {
}
