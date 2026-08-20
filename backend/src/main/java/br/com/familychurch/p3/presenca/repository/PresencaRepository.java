package br.com.familychurch.p3.presenca.repository;

import br.com.familychurch.p3.presenca.Presenca;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PresencaRepository extends JpaRepository<Presenca, UUID> {

	boolean existsByParticipanteIdAndAulaId(UUID participanteId, UUID aulaId);

	Optional<Presenca> findByParticipanteIdAndAulaId(UUID participanteId, UUID aulaId);

	List<Presenca> findByAulaIdOrderByTimestampCheckinAsc(UUID aulaId);

	long countByAulaId(UUID aulaId);

	boolean existsByParticipanteIdAndAula_TipoAulaId(UUID participanteId, UUID tipoAulaId);

}
