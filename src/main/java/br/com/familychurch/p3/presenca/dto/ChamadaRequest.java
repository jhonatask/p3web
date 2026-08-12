package br.com.familychurch.p3.presenca.dto;

import br.com.familychurch.p3.common.validation.Cpf;

public record ChamadaRequest(

		@Cpf
		String cpf

) {
}
