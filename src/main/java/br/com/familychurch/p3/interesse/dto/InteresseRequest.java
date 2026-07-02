package br.com.familychurch.p3.interesse.dto;

import br.com.familychurch.p3.aluno.validation.Cpf;

public record InteresseRequest(

		@Cpf
		String cpf,

		boolean gcHomens,

		boolean proximoAwake,

		boolean discipulado

) {
}
