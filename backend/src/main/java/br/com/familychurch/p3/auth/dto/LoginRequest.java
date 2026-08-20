package br.com.familychurch.p3.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(

		@NotBlank
		String email,

		@NotBlank
		String senha

) {
}
