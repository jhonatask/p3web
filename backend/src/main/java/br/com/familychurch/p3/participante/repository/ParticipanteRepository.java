package br.com.familychurch.p3.participante.repository;

import br.com.familychurch.p3.participante.Participante;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ParticipanteRepository extends JpaRepository<Participante, UUID> {

	Optional<Participante> findByCpf(String cpf);

	boolean existsByCpf(String cpf);

}
