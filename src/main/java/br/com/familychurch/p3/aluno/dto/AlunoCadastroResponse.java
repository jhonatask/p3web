package br.com.familychurch.p3.aluno.dto;

import br.com.familychurch.p3.aluno.Aluno;
import java.util.UUID;

public record AlunoCadastroResponse(

		UUID id,
		String nome

) {

	public static AlunoCadastroResponse from(Aluno aluno) {
		return new AlunoCadastroResponse(aluno.getId(), aluno.getNome());
	}

}
