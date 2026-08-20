package br.com.familychurch.p3.turma.repository;

import br.com.familychurch.p3.turma.StatusTurma;
import br.com.familychurch.p3.turma.Turma;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TurmaRepository extends JpaRepository<Turma, UUID> {

	Optional<Turma> findByTokenCadastro(String tokenCadastro);

	boolean existsByTokenCadastro(String tokenCadastro);

	List<Turma> findByCursoId(UUID cursoId);

	List<Turma> findByStatus(StatusTurma status);

	List<Turma> findByCursoIdAndStatus(UUID cursoId, StatusTurma status);

}
