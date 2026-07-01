package br.com.familychurch.p3.common.exception;

import java.time.Instant;
import java.util.List;

public record ApiError(String mensagem, int status, Instant timestamp, List<CampoInvalido> erros) {

	public static ApiError de(String mensagem, int status) {
		return new ApiError(mensagem, status, Instant.now(), null);
	}

	public static ApiError deValidacao(String mensagem, int status, List<CampoInvalido> erros) {
		return new ApiError(mensagem, status, Instant.now(), erros);
	}

	public record CampoInvalido(String campo, String mensagem) {
	}

}
