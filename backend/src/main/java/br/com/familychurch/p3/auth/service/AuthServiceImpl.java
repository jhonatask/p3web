package br.com.familychurch.p3.auth.service;

import br.com.familychurch.p3.auth.JwtService;
import br.com.familychurch.p3.auth.dto.LoginRequest;
import br.com.familychurch.p3.auth.dto.LoginResponse;
import br.com.familychurch.p3.auth.exception.CredenciaisInvalidasException;
import br.com.familychurch.p3.usuarioadmin.UsuarioAdmin;
import br.com.familychurch.p3.usuarioadmin.repository.UsuarioAdminRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

	private final UsuarioAdminRepository usuarioAdminRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;

	public AuthServiceImpl(UsuarioAdminRepository usuarioAdminRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
		this.usuarioAdminRepository = usuarioAdminRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtService = jwtService;
	}

	@Override
	public LoginResponse login(LoginRequest request) {
		UsuarioAdmin usuarioAdmin = usuarioAdminRepository.findByEmailIgnoreCase(request.email())
				.orElseThrow(CredenciaisInvalidasException::new);

		if (!passwordEncoder.matches(request.senha(), usuarioAdmin.getSenha())) {
			throw new CredenciaisInvalidasException();
		}

		String token = jwtService.gerarToken(usuarioAdmin.getEmail(), usuarioAdmin.getPapel().name(), usuarioAdmin.getNome());
		return new LoginResponse(token, usuarioAdmin.getNome(), usuarioAdmin.getPapel());
	}

}
