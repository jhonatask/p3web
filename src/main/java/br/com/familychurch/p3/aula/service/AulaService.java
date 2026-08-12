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

	/**
	 * Desativa a(s) aula(s) ATIVA(s) sem abrir a proxima - diferente de
	 * {@link #encerrarAulaAtual(UUID)}, que sempre avanca. Fica sem nenhuma
	 * aula ativa ate o lider definir/abrir outra data ou avancar
	 * manualmente. Lanca NenhumaAulaAtivaException se nao havia nenhuma
	 * aula ativa pra desativar.
	 */
	void desativarAulaAtual(UUID turmaId);

}
