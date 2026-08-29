package br.com.familychurch.p3.aula.repository;

import br.com.familychurch.p3.aula.Aula;
import br.com.familychurch.p3.aula.StatusAula;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AulaRepository extends JpaRepository<Aula, UUID> {

	List<Aula> findByTurmaId(UUID turmaId);

	List<Aula> findByTurmaIdOrderByTipoAulaOrdemAsc(UUID turmaId);

	/**
	 * Em uso normal ha no maximo uma aula ATIVA por turma (a agenda tem datas
	 * distintas). Mas nada no banco impede o lider de agendar duas aulas para
	 * a mesma data por engano - devolve List (nao Optional/single result)
	 * para nao quebrar em runtime nesse caso; AulaServiceImpl decide
	 * deterministicamente qual usar.
	 */
	List<Aula> findByTurmaIdAndStatusOrderByTipoAulaOrdemAsc(UUID turmaId, StatusAula status);

	Optional<Aula> findByTurmaIdAndTipoAulaId(UUID turmaId, UUID tipoAulaId);

	Optional<Aula> findFirstByTurmaIdOrderByTipoAulaOrdemAsc(UUID turmaId);

}
