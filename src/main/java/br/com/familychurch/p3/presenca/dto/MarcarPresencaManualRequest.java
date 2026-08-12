package br.com.familychurch.p3.presenca.dto;

import br.com.familychurch.p3.common.validation.Cpf;

public record MarcarPresencaManualRequest(

		@Cpf
		String cpf

) {
}
