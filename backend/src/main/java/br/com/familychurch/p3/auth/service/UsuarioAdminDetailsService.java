package br.com.familychurch.p3.auth.service;

import br.com.familychurch.p3.auth.UsuarioAdminDetails;
import br.com.familychurch.p3.usuarioadmin.UsuarioAdmin;
import br.com.familychurch.p3.usuarioadmin.repository.UsuarioAdminRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UsuarioAdminDetailsService implements UserDetailsService {

	private final UsuarioAdminRepository usuarioAdminRepository;

	public UsuarioAdminDetailsService(UsuarioAdminRepository usuarioAdminRepository) {
		this.usuarioAdminRepository = usuarioAdminRepository;
	}

	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		UsuarioAdmin usuarioAdmin = usuarioAdminRepository.findByEmailIgnoreCase(email)
				.orElseThrow(() -> new UsernameNotFoundException("Usuario nao encontrado para o email '%s'".formatted(email)));
		return new UsuarioAdminDetails(usuarioAdmin);
	}

}
