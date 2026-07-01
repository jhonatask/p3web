package br.com.familychurch.p3.curso;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CursoRepository extends JpaRepository<Curso, UUID> {

	Optional<Curso> findByNomeIgnoreCase(String nome);

	List<Curso> findByAtivo(boolean ativo);

}
