package br.com.familychurch.p3;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;

import br.com.familychurch.p3.auth.PessoaAuthenticationToken;
import br.com.familychurch.p3.auth.PessoaPrincipal;
import br.com.familychurch.p3.aula.repository.TipoAulaRepository;
import br.com.familychurch.p3.curso.repository.CursoRepository;
import br.com.familychurch.p3.usuarioadmin.repository.UsuarioAdminRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

@Tag("integration")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
public abstract class IntegrationTestSupport {

	protected static final String SUB_ADMIN_SEMENTE = "11111111-1111-1111-1111-111111111111";
	protected static final UUID PESSOA_ID_ADMIN_SEMENTE = UUID.fromString("22222222-2222-2222-2222-222222222222");
	protected static final UUID ID_ADMIN_SEMENTE = UUID.fromString("ca287c71-0602-4690-a88a-b8047c997723");
	protected static final String NOME_CURSO_ESCOLA_HOMBRIDADE = "Escola de Hombridade";

	protected final ObjectMapper objectMapper = new ObjectMapper();

	@Autowired
	private CursoRepository cursoRepository;

	@Autowired
	private TipoAulaRepository tipoAulaRepository;

	@Autowired
	private UsuarioAdminRepository usuarioAdminRepository;

	@ServiceConnection
	static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16");

	static {
		POSTGRES.start();
	}

	@BeforeEach
	void vincularPessoaAoAdminSemente() {
		usuarioAdminRepository.findById(ID_ADMIN_SEMENTE).ifPresent(admin -> {
			if (admin.getPessoaId() == null) {
				admin.setPessoaId(PESSOA_ID_ADMIN_SEMENTE);
				usuarioAdminRepository.save(admin);
			}
		});
	}

	protected RequestPostProcessor autenticado() {
		Jwt jwt = Jwt.withTokenValue("token-de-teste")
				.header("alg", "none")
				.subject(SUB_ADMIN_SEMENTE)
				.claim("sub", SUB_ADMIN_SEMENTE)
				.build();
		PessoaPrincipal principal = new PessoaPrincipal(PESSOA_ID_ADMIN_SEMENTE, SUB_ADMIN_SEMENTE);
		return authentication(new PessoaAuthenticationToken(principal, jwt, List.of()));
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
