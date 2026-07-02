package br.com.familychurch.p3.avaliacao.repository;

import br.com.familychurch.p3.avaliacao.Avaliacao;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AvaliacaoRepository extends JpaRepository<Avaliacao, UUID> {

	boolean existsByAlunoIdAndAulaId(UUID alunoId, UUID aulaId);

	List<Avaliacao> findByAulaId(UUID aulaId);

	@Query("select avg(a.nota) from Avaliacao a where a.aula.id = :aulaId")
	Double mediaNotaPorAula(@Param("aulaId") UUID aulaId);

	@Query("select avg(a.nps) from Avaliacao a where a.aula.id = :aulaId")
	Double mediaNpsPorAula(@Param("aulaId") UUID aulaId);

}
