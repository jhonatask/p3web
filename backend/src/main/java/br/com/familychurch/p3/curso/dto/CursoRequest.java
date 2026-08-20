package br.com.familychurch.p3.curso.dto;

import jakarta.validation.constraints.NotBlank;

public record CursoRequest(

		@NotBlank
		String nome,

		String descricao

) {
}
