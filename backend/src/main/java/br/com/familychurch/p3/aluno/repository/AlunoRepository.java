package br.com.familychurch.p3.aluno.repository;

import br.com.familychurch.p3.aluno.Aluno;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlunoRepository extends JpaRepository<Aluno, UUID> {

	Optional<Aluno> findByParticipanteId(UUID participanteId);

	List<Aluno> findByTurmaId(UUID turmaId);

	long countByTurmaId(UUID turmaId);

}
