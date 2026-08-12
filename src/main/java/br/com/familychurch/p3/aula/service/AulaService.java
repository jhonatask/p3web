package br.com.familychurch.p3.aula.service;

import br.com.familychurch.p3.aula.dto.AgendaRequest;
import br.com.familychurch.p3.aula.dto.AulaResponse;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AulaService {

	List<AulaResponse> definirAgenda(UUID turmaId, AgendaRequest request);

	List<AulaResponse> listarPorTurma(UUID turmaId);

	AulaResponse buscarPorId(UUID id);

	AulaResponse buscarAulaAtiva(UUID turmaId);

	AulaResponse buscarAulaAtivaPorToken(String tokenTurma);

	void sincronizarStatusPorData(UUID turmaId);

	/**
	 * Avanco manual (alternativa ao automatico por data): encerra a(s)
	 * aula(s) ATIVA(s) e abre a proxima AGENDADA por ordem do catalogo,
	 * independente da data agendada. Retorna a nova aula ativa, ou vazio se
	 * a encerrada era a ultima da agenda.
	 */
	Optional<AulaResponse> encerrarAulaAtual(UUID turmaId);

}
