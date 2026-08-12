package br.com.familychurch.p3.aula.service;

import br.com.familychurch.p3.aula.dto.AgendaRequest;
import br.com.familychurch.p3.aula.dto.AulaResponse;
import java.util.List;
import java.util.UUID;

public interface AulaService {

	List<AulaResponse> definirAgenda(UUID turmaId, AgendaRequest request);

	List<AulaResponse> listarPorTurma(UUID turmaId);

	AulaResponse buscarPorId(UUID id);

	AulaResponse buscarAulaAtiva(UUID turmaId);

	AulaResponse buscarAulaAtivaPorToken(String tokenTurma);

	void sincronizarStatusPorData(UUID turmaId);

}
