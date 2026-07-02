package br.com.familychurch.p3.interesse.repository;

import br.com.familychurch.p3.interesse.Interesse;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InteresseRepository extends JpaRepository<Interesse, UUID> {

	boolean existsByAlunoIdAndAulaId(UUID alunoId, UUID aulaId);

	List<Interesse> findByAulaTurmaId(UUID turmaId);

}
