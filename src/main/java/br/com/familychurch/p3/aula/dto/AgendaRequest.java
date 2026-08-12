package br.com.familychurch.p3.aula.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record AgendaRequest(

		@NotEmpty
		@Valid
		List<AgendaItemRequest> itens

) {
}
