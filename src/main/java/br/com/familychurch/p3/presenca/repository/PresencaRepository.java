package br.com.familychurch.p3.presenca.repository;

import br.com.familychurch.p3.presenca.Presenca;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PresencaRepository extends JpaRepository<Presenca, UUID> {

	boolean existsByAlunoIdAndAulaId(UUID alunoId, UUID aulaId);

	List<Presenca> findByAulaIdOrderByTimestampCheckinAsc(UUID aulaId);

	long countByAulaId(UUID aulaId);

}
