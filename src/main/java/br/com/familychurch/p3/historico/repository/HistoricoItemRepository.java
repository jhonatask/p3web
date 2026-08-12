package br.com.familychurch.p3.historico.repository;

import br.com.familychurch.p3.historico.HistoricoItem;
import br.com.familychurch.p3.historico.OrigemHistorico;
import br.com.familychurch.p3.historico.TipoHistorico;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HistoricoItemRepository extends JpaRepository<HistoricoItem, UUID> {

	List<HistoricoItem> findByParticipanteId(UUID participanteId);

	boolean existsByParticipanteIdAndItemAndTipoAndOrigem(
			UUID participanteId, String item, TipoHistorico tipo, OrigemHistorico origem);

}
