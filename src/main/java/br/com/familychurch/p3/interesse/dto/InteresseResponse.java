package br.com.familychurch.p3.interesse.dto;

import br.com.familychurch.p3.interesse.Interesse;
import java.util.UUID;

public record InteresseResponse(

		UUID id,
		UUID alunoId,
		String alunoNome,
		UUID aulaId,
		boolean gcHomens,
		boolean proximoAwake,
		boolean discipulado

) {

	public static InteresseResponse from(Interesse interesse) {
		return new InteresseResponse(
				interesse.getId(),
				interesse.getAluno().getId(),
				interesse.getAluno().getNome(),
				interesse.getAula().getId(),
				interesse.isGcHomens(),
				interesse.isProximoAwake(),
				interesse.isDiscipulado());
	}

}
