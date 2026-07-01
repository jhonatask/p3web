package br.com.familychurch.p3.aluno.dto;

import br.com.familychurch.p3.aluno.Aluno;
import java.time.LocalDateTime;
import java.util.UUID;

public record AlunoResponse(

		UUID id,
		String nome,
		String cpf,
		String celular,
		String campus,
		UUID turmaId,
		LocalDateTime dataCadastro,
		boolean consentimentoLgpd,
		LocalDateTime consentimentoLgpdTimestamp

) {

	public static AlunoResponse from(Aluno aluno) {
		return new AlunoResponse(
				aluno.getId(),
				aluno.getNome(),
				aluno.getCpf(),
				aluno.getCelular(),
				aluno.getCampus(),
				aluno.getTurma().getId(),
				aluno.getDataCadastro(),
				aluno.isConsentimentoLgpd(),
				aluno.getConsentimentoLgpdTimestamp());
	}

}
