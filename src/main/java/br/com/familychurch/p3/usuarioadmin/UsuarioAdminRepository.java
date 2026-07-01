package br.com.familychurch.p3.usuarioadmin;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioAdminRepository extends JpaRepository<UsuarioAdmin, UUID> {

	Optional<UsuarioAdmin> findByEmailIgnoreCase(String email);

}
