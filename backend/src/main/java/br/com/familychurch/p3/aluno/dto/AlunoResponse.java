package br.com.familychurch.p3.aluno.dto;

import br.com.familychurch.p3.aluno.Aluno;
import java.time.LocalDateTime;
import java.util.UUID;

public record AlunoResponse(

		UUID id,
		UUID participanteId,
		String nome,
		String cpf,
		String celular,
		String campus,
		UUID turmaId,
		LocalDateTime dataMatricula

) {

	public static AlunoResponse from(Aluno aluno) {
		return new AlunoResponse(
				aluno.getId(),
				aluno.getParticipante().getId(),
				aluno.getParticipante().getNome(),
				aluno.getParticipante().getCpf(),
				aluno.getParticipante().getCelular(),
				aluno.getParticipante().getCampus(),
				aluno.getTurma().getId(),
				aluno.getDataMatricula());
	}

}
