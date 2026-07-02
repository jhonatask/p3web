package br.com.familychurch.p3.turma.service;

import br.com.familychurch.p3.turma.StatusTurma;
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

}
