package br.com.familychurch.p3.aluno.dto;

import br.com.familychurch.p3.aluno.validation.Cpf;
import jakarta.validation.constraints.NotBlank;

public record AlunoCadastroRequest(

		@NotBlank
		String nome,

		@Cpf
		String cpf,

		@NotBlank
		String celular,

		@NotBlank
		String campus,

		boolean aceiteLgpd

) {
}
