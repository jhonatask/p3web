package br.com.familychurch.p3.historico.dto;

import br.com.familychurch.p3.historico.HistoricoItem;
import br.com.familychurch.p3.historico.OrigemHistorico;
import br.com.familychurch.p3.historico.StatusHistorico;
import br.com.familychurch.p3.historico.TipoHistorico;
import java.time.LocalDate;
import java.util.UUID;

public record HistoricoItemResponse(

		UUID id,
		String item,
		TipoHistorico tipo,
		String turma,
		StatusHistorico status,
		LocalDate data,
		OrigemHistorico origem

) {

	public static HistoricoItemResponse from(HistoricoItem historicoItem) {
		return new HistoricoItemResponse(
				historicoItem.getId(),
				historicoItem.getItem(),
				historicoItem.getTipo(),
				historicoItem.getTurma(),
				historicoItem.getStatus(),
				historicoItem.getData(),
				historicoItem.getOrigem());
	}

}
