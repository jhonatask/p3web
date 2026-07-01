package br.com.familychurch.p3.auth;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
class AuthServiceImpl implements AuthService {

	private final UsuarioAdminRepository usuarioAdminRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;

	AuthServiceImpl(UsuarioAdminRepository usuarioAdminRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
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

		String token = jwtService.gerarToken(usuarioAdmin);
		return new LoginResponse(token, usuarioAdmin.getNome(), usuarioAdmin.getPapel());
	}

}
