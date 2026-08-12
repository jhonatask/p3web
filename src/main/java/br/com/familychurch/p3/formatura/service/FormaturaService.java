package br.com.familychurch.p3.formatura.service;

import br.com.familychurch.p3.formatura.dto.LinkWhatsappResponse;
import br.com.familychurch.p3.formatura.dto.PainelTurmaResponse;
import br.com.familychurch.p3.formatura.dto.StatusFormatura;
import java.util.UUID;

public interface FormaturaService {

	StatusFormatura calcularStatus(UUID participanteId);

	/**
	 * Recalcula o status de formatura do participante e, se apto (no maximo
	 * 1 falta por modulo), grava/atualiza a conclusao na entidade Historico
	 * (write-back pelo CPF, Entrega 1). Chamado apos toda gravacao de
	 * Presenca - inclusive reposicoes, que podem reverter um "nao forma".
	 */
	void recalcularEGravarConclusao(UUID participanteId);

	/**
	 * Painel do lider: status de formatura de cada participante matriculado
	 * na turma.
	 */
	PainelTurmaResponse obterPainel(UUID turmaId);

	/**
	 * Link wa.me pronto para o lider notificar o participante sobre a(s)
	 * aula(s) pendente(s) de reposicao.
	 */
	LinkWhatsappResponse gerarLinkWhatsapp(UUID turmaId, UUID participanteId);

}
