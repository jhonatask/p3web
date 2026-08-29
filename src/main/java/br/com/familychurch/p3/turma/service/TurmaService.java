package br.com.familychurch.p3.turma.service;

import br.com.familychurch.p3.turma.StatusTurma;
import br.com.familychurch.p3.turma.dto.TurmaAbertaResponse;
import br.com.familychurch.p3.turma.dto.TurmaDashboardResponse;
import br.com.familychurch.p3.turma.dto.TurmaPublicaResponse;
import br.com.familychurch.p3.turma.dto.TurmaRequest;
import br.com.familychurch.p3.turma.dto.TurmaResponse;
import java.util.List;
import java.util.UUID;

public interface TurmaService {

	TurmaResponse criar(TurmaRequest request);

	List<TurmaResponse> listar(UUID cursoId, StatusTurma status);

	TurmaResponse buscarPorId(UUID id);

	TurmaResponse encerrar(UUID id);

	TurmaPublicaResponse buscarPorTokenCadastro(String token);

	TurmaDashboardResponse obterDashboard(UUID turmaId);

	byte[] gerarImagemQrCode(UUID turmaId);

	List<TurmaAbertaResponse> listarAbertasParaMatricula();

	/**
	 * Turma aceita matricula direta (fora do fluxo de chamada) se estiver
	 * ATIVA e a primeira aula da agenda (menor {@code TipoAula.ordem}) ainda
	 * nao tiver comecado - sem agenda definida, conta como ainda nao
	 * comecada.
	 */
	boolean aceitaMatriculaDireta(UUID turmaId);

}
