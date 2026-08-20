package br.com.familychurch.p3.auth;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JwtServiceTest {

	private JwtService jwtService;

	@BeforeEach
	void configurar() {
		JwtProperties jwtProperties = new JwtProperties(
				"chave-secreta-de-teste-com-tamanho-suficiente-para-hmac-sha-1234567890", 60);
		jwtService = new JwtService(jwtProperties);
	}

	@Test
	void deveGerarTokenEValidarComoTrue() {
		String token = jwtService.gerarToken("admin@hombridade.local", "ADMIN", "Administrador Local");

		assertThat(jwtService.tokenValido(token)).isTrue();
	}

	@Test
	void deveRetornarFalsoParaTokenAdulterado() {
		String token = jwtService.gerarToken("admin@hombridade.local", "ADMIN", "Administrador Local");
		String[] partes = token.split("\\.");
		String assinaturaAdulterada = new StringBuilder(partes[2]).reverse().toString();
		String tokenAdulterado = "%s.%s.%s".formatted(partes[0], partes[1], assinaturaAdulterada);

		assertThat(jwtService.tokenValido(tokenAdulterado)).isFalse();
	}

	@Test
	void deveRetornarFalsoParaTokenMalformado() {
		assertThat(jwtService.tokenValido("token-completamente-invalido")).isFalse();
	}

	@Test
	void deveExtrairEmailDoSubjectDoToken() {
		String token = jwtService.gerarToken("admin@hombridade.local", "ADMIN", "Administrador Local");

		assertThat(jwtService.extrairEmail(token)).isEqualTo("admin@hombridade.local");
	}

}
