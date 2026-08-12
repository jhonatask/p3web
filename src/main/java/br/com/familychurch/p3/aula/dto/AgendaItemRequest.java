package br.com.familychurch.p3.aula.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

public record AgendaItemRequest(

		@NotNull
		UUID tipoAulaId,

		@NotNull
		LocalDate data

) {
}
