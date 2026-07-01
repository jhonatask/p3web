package br.com.familychurch.p3.auth.controller;

import br.com.familychurch.p3.auth.dto.LoginRequest;
import br.com.familychurch.p3.auth.dto.LoginResponse;
import br.com.familychurch.p3.auth.exception.CredenciaisInvalidasException;
import br.com.familychurch.p3.auth.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private final AuthService authService;

	public AuthController(AuthService authService) {
		this.authService = authService;
	}

	@PostMapping("/login")
	public LoginResponse login(@Valid @RequestBody LoginRequest request) {
		return authService.login(request);
	}

	@ExceptionHandler(CredenciaisInvalidasException.class)
	public ResponseEntity<String> tratarCredenciaisInvalidas(CredenciaisInvalidasException exception) {
		return ResponseEntity.status(401).body(exception.getMessage());
	}

}
