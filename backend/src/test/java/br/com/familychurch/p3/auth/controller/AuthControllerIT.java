package br.com.familychurch.p3.auth.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.familychurch.p3.IntegrationTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

class AuthControllerIT extends IntegrationTestSupport {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void deveAutenticarComCredenciaisCorretasERetornarToken() throws Exception {
		mockMvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"email": "%s", "senha": "%s"}
						""".formatted(EMAIL_ADMIN_SEMENTE, SENHA_ADMIN_SEMENTE)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.token").isNotEmpty())
				.andExpect(jsonPath("$.papel").value("ADMIN"));
	}

	@Test
	void deveRetornarNaoAutorizadoQuandoSenhaIncorreta() throws Exception {
		mockMvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"email": "%s", "senha": "senha-errada"}
						""".formatted(EMAIL_ADMIN_SEMENTE)))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void deveNegarAcessoAEndpointProtegidoSemToken() throws Exception {
		mockMvc.perform(get("/api/cursos"))
				.andExpect(status().is4xxClientError());
	}

	@Test
	void devePermitirAcessoAEndpointProtegidoComTokenValido() throws Exception {
		String token = obterTokenAdmin();

		mockMvc.perform(get("/api/cursos")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
				.andExpect(status().isOk());
	}

}
