package br.com.familychurch.p3.aula.repository;

import br.com.familychurch.p3.aula.Aula;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AulaRepository extends JpaRepository<Aula, UUID> {

	Optional<Aula> findByQrToken(String qrToken);

	boolean existsByQrToken(String qrToken);

	List<Aula> findByTurmaId(UUID turmaId);

	List<Aula> findByTurmaIdOrderByNumeroAsc(UUID turmaId);

}
