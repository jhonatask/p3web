package br.com.familychurch.p3.turma.dto;

import br.com.familychurch.p3.turma.Turma;
import java.util.UUID;

public record TurmaAbertaResponse(

		UUID id,
		String cursoNome,
		String nome,
		String moduloAtual

) {

	public static TurmaAbertaResponse from(Turma turma) {
		return new TurmaAbertaResponse(turma.getId(), turma.getCurso().getNome(), turma.getNome(), turma.getModuloAtual());
	}

}
