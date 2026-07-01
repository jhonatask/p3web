package br.com.familychurch.p3.curso;

import java.util.UUID;

public record CursoResponse(

		UUID id,
		String nome,
		String descricao,
		boolean ativo

) {

	static CursoResponse from(Curso curso) {
		return new CursoResponse(curso.getId(), curso.getNome(), curso.getDescricao(), curso.isAtivo());
	}

}
