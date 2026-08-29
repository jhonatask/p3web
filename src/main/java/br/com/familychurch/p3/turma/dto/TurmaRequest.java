package br.com.familychurch.p3.turma.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record TurmaRequest(

		@NotNull
		UUID cursoId,

		@NotBlank
		String nome,

		String moduloAtual,

		UUID liderId

) {
}
