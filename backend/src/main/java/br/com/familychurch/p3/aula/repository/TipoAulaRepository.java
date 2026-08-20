package br.com.familychurch.p3.aula.repository;

import br.com.familychurch.p3.aula.TipoAula;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TipoAulaRepository extends JpaRepository<TipoAula, UUID> {

	List<TipoAula> findByCursoIdOrderByOrdemAsc(UUID cursoId);

}
