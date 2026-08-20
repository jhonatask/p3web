package br.com.familychurch.p3.presenca.service;

import br.com.familychurch.p3.presenca.dto.ChamadaRequest;
import br.com.familychurch.p3.presenca.dto.MarcarPresencaManualRequest;
import br.com.familychurch.p3.presenca.dto.PresencaResponse;
import java.util.List;
import java.util.UUID;

public interface PresencaService {

	/**
	 * Registra a presenca do participante (por CPF) na aula ativa da turma do
	 * token. Idempotente: se ja existe presenca para essa aula, so retorna a
	 * existente em vez de erro (reentrancia do formulario). Cria a matricula
	 * do participante automaticamente se essa for a primeira chamada dele em
	 * qualquer turma.
	 */
	PresencaResponse registrarChamada(String tokenTurma, ChamadaRequest request);

	PresencaResponse marcarManual(UUID aulaId, MarcarPresencaManualRequest request, UUID professorId);

	List<PresencaResponse> listarPorAula(UUID aulaId);

}
