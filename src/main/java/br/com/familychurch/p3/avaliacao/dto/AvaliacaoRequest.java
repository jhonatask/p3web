package br.com.familychurch.p3.avaliacao.dto;

import br.com.familychurch.p3.aluno.validation.Cpf;
import br.com.familychurch.p3.avaliacao.DidaticaAvaliacao;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record AvaliacaoRequest(

		@Cpf
		String cpf,

		@NotNull
		@Min(0)
		@Max(10)
		Integer nota,

		@NotNull
		DidaticaAvaliacao didatica,

		String sugestao,

		@NotNull
		@Min(0)
		@Max(10)
		Integer nps

) {
}
