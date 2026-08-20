package br.com.familychurch.p3.auth.service;

import br.com.familychurch.p3.auth.dto.LoginRequest;
import br.com.familychurch.p3.auth.dto.LoginResponse;

public interface AuthService {

	LoginResponse login(LoginRequest request);

}
