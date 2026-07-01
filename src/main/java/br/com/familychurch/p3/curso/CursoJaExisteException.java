package br.com.familychurch.p3.curso;

public class CursoJaExisteException extends RuntimeException {

	public CursoJaExisteException(String nome) {
		super("Ja existe um curso ativo com o nome '%s'".formatted(nome));
	}

}
