package br.com.familychurch.p3.turma.dto;

import br.com.familychurch.p3.turma.StatusTurma;
import br.com.familychurch.p3.turma.Turma;

public record TurmaPublicaResponse(

		String cursoNome,
		String turmaNome,
		String moduloAtual,
		StatusTurma status

) {

	public static TurmaPublicaResponse from(Turma turma) {
		return new TurmaPublicaResponse(
				turma.getCurso().getNome(),
				turma.getNome(),
				turma.getModuloAtual(),
				turma.getStatus());
	}

}
