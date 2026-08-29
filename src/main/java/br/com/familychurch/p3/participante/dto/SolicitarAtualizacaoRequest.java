package br.com.familychurch.p3.participante.dto;

import br.com.familychurch.p3.common.validation.Cpf;

public record SolicitarAtualizacaoRequest(

		@Cpf
		String cpf

) {
}
