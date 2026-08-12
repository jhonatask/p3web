package br.com.familychurch.p3;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import br.com.familychurch.p3.aula.repository.TipoAulaRepository;
import br.com.familychurch.p3.curso.repository.CursoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

@Tag("integration")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
public abstract class IntegrationTestSupport {

	protected static final String EMAIL_ADMIN_SEMENTE = "admin@hombridade.local";
	protected static final String SENHA_ADMIN_SEMENTE = "hombridade2026";
	protected static final String NOME_CURSO_ESCOLA_HOMBRIDADE = "Escola de Hombridade";

	protected final ObjectMapper objectMapper = new ObjectMapper();

	@Autowired
	private MockMvc mockMvcParaLogin;

	@Autowired
	private CursoRepository cursoRepository;

	@Autowired
	private TipoAulaRepository tipoAulaRepository;

	@ServiceConnection
	static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16");

	static {
		POSTGRES.start();
	}

	protected String obterTokenAdmin() throws Exception {
		var resultado = mockMvcParaLogin.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"email": "%s", "senha": "%s"}
						""".formatted(EMAIL_ADMIN_SEMENTE, SENHA_ADMIN_SEMENTE)))
				.andReturn();
		return objectMapper.readTree(resultado.getResponse().getContentAsString()).get("token").asText();
	}

	/**
	 * O curso "Escola de Hombridade" e seu catalogo de TipoAula sao dado de
	 * referencia semeado pela migration V15 (nao dado de teste) - nunca
	 * apagados entre testes, so reaproveitados.
	 */
	protected UUID obterCursoEscolaHombridade() {
		return cursoRepository.findByNomeIgnoreCase(NOME_CURSO_ESCOLA_HOMBRIDADE).orElseThrow().getId();
	}

	protected UUID obterTipoAulaPorNumero(int numero) {
		return tipoAulaRepository.findByCursoIdOrderByOrdemAsc(obterCursoEscolaHombridade()).stream()
				.filter(tipoAula -> tipoAula.getNumero().equals(numero))
				.findFirst()
				.orElseThrow()
				.getId();
	}

}
