package br.com.familychurch.p3.usuarioadmin.repository;

import br.com.familychurch.p3.usuarioadmin.Papel;
import br.com.familychurch.p3.usuarioadmin.UsuarioAdmin;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioAdminRepository extends JpaRepository<UsuarioAdmin, UUID> {

	Optional<UsuarioAdmin> findByEmailIgnoreCase(String email);

	Optional<UsuarioAdmin> findByPessoaId(UUID pessoaId);

	List<UsuarioAdmin> findByPapel(Papel papel);

}
