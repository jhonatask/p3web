package br.com.familychurch.p3.aula.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record AulaRequest(

		@NotNull
		Integer numero,

		String modulo,

		@NotNull
		LocalDate data,

		@NotNull
		LocalDateTime janelaAbertura,

		@NotNull
		LocalDateTime janelaFechamento

) {
}
