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
		StatusTurma status,
		UUID liderId,
		String liderNome

) {

	public static TurmaResponse from(Turma turma, String baseUrlChamada) {
		String link = "%s/%s".formatted(baseUrlChamada, turma.getTokenCadastro());
		return new TurmaResponse(
				turma.getId(),
				turma.getCurso().getId(),
				turma.getCurso().getNome(),
				turma.getNome(),
				turma.getModuloAtual(),
				turma.getTokenCadastro(),
				link,
				turma.getStatus(),
				turma.getLider() != null ? turma.getLider().getId() : null,
				turma.getLider() != null ? turma.getLider().getNome() : null);
	}

}
