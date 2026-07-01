package br.com.familychurch.p3.turma.dto;

import br.com.familychurch.p3.turma.StatusTurma;
import br.com.familychurch.p3.turma.Turma;
import java.util.UUID;

public record TurmaResponse(

		UUID id,
		UUID cursoId,
		String cursoNome,
		String nome,
		String moduloAtual,
		String tokenCadastro,
		String linkCadastro,
		StatusTurma status

) {

	public static TurmaResponse from(Turma turma, String baseUrlCadastro) {
		String link = "%s/%s".formatted(baseUrlCadastro, turma.getTokenCadastro());
		return new TurmaResponse(
				turma.getId(),
				turma.getCurso().getId(),
				turma.getCurso().getNome(),
				turma.getNome(),
				turma.getModuloAtual(),
				turma.getTokenCadastro(),
				link,
				turma.getStatus());
	}

}
