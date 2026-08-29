package br.com.familychurch.p3.aluno.service;

import br.com.familychurch.p3.aluno.Aluno;
import br.com.familychurch.p3.aluno.dto.AlunoResponse;
import br.com.familychurch.p3.aluno.dto.MatriculaResponse;
import java.util.List;
import java.util.UUID;

public interface AlunoService {

	/**
	 * Retorna a matricula do participante, criando-a se essa for a primeira
	 * chamada dele em qualquer turma. Idempotente: chamadas seguintes (mesma
	 * turma ou reposicao em outra) so leem a matricula existente.
	 */
	Aluno matricularSeNecessario(UUID participanteId, UUID turmaId);

	List<AlunoResponse> listarPorTurma(UUID turmaId);

	/**
	 * Matricula direta fora do fluxo de chamada - usada logo apos o cadastro
	 * base, quando o participante escolhe uma turma aberta em vez de esperar
	 * o link de uma aula. So aceita turmas que ainda nao comecaram (ver
	 * {@link br.com.familychurch.p3.turma.service.TurmaService#aceitaMatriculaDireta}).
	 */
	MatriculaResponse matricularDireto(String cpf, String token, UUID turmaId);

}
