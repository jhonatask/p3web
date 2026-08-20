package br.com.familychurch.p3.turma.controller;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.familychurch.p3.IntegrationTestSupport;
import br.com.familychurch.p3.aluno.repository.AlunoRepository;
import br.com.familychurch.p3.aula.repository.AulaRepository;
import br.com.familychurch.p3.avaliacao.repository.AvaliacaoRepository;
import br.com.familychurch.p3.curso.Curso;
import br.com.familychurch.p3.curso.repository.CursoRepository;
import br.com.familychurch.p3.presenca.repository.PresencaRepository;
import br.com.familychurch.p3.turma.repository.TurmaRepository;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

class TurmaControllerIT extends IntegrationTestSupport {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private CursoRepository cursoRepository;

	@Autowired
	private TurmaRepository turmaRepository;

	@Autowired
	private AlunoRepository alunoRepository;

	@Autowired
	private AulaRepository aulaRepository;

	@Autowired
	private PresencaRepository presencaRepository;

	@Autowired
	private AvaliacaoRepository avaliacaoRepository;

	private String tokenAdmin;

	@BeforeEach
	void configurarCenario() throws Exception {
		avaliacaoRepository.deleteAll();
		presencaRepository.deleteAll();
		aulaRepository.deleteAll();
		alunoRepository.deleteAll();
		turmaRepository.deleteAll();
		tokenAdmin = obterTokenAdmin();
	}

	@Test
	void deveCriarTurmaVinculadaACursoAtivoComTokenUnico() throws Exception {
		UUID cursoId = obterCursoEscolaHombridade();

		mockMvc.perform(post("/api/turmas")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAdmin)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"cursoId": "%s", "nome": "Campus 85 - Turma 13", "moduloAtual": "Provisao"}
						""".formatted(cursoId)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.nome").value("Campus 85 - Turma 13"))
				.andExpect(jsonPath("$.status").value("ATIVA"))
				.andExpect(jsonPath("$.tokenCadastro").isNotEmpty())
				.andExpect(jsonPath("$.linkCadastro").value(containsString("/")));
	}

	@Test
	void deveRetornar404QuandoCursoInexistente() throws Exception {
		UUID cursoInexistente = UUID.randomUUID();

		mockMvc.perform(post("/api/turmas")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAdmin)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"cursoId": "%s", "nome": "Turma qualquer"}
						""".formatted(cursoInexistente)))
				.andExpect(status().isNotFound());
	}

	@Test
	void deveRetornar409QuandoCursoInativo() throws Exception {
		UUID cursoId = criarCursoAtivo("Curso a desativar");
		mockMvc.perform(patch("/api/cursos/{id}/desativar", cursoId)
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAdmin));

		mockMvc.perform(post("/api/turmas")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAdmin)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"cursoId": "%s", "nome": "Turma sobre curso inativo"}
						""".formatted(cursoId)))
				.andExpect(status().isConflict());
	}

	@Test
	void deveListarTurmasComFiltroDeStatus() throws Exception {
		UUID cursoId = obterCursoEscolaHombridade();
		String idTurmaEncerrada = criarTurma(cursoId, "Turma a encerrar");
		criarTurma(cursoId, "Turma ativa");
		mockMvc.perform(patch("/api/turmas/{id}/encerrar", idTurmaEncerrada)
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAdmin));

		mockMvc.perform(get("/api/turmas").param("status", "ATIVA")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAdmin))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].nome").value("Turma ativa"));
	}

	@Test
	void deveEncerrarTurma() throws Exception {
		UUID cursoId = obterCursoEscolaHombridade();
		String turmaId = criarTurma(cursoId, "Turma a encerrar");

		mockMvc.perform(patch("/api/turmas/{id}/encerrar", turmaId)
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAdmin))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("ENCERRADA"));
	}

	@Test
	void deveObterDashboardDeTurmaSemAulas() throws Exception {
		UUID cursoId = obterCursoEscolaHombridade();
		String turmaId = criarTurma(cursoId, "Turma sem aulas");

		mockMvc.perform(get("/api/turmas/{turmaId}/dashboard", turmaId)
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAdmin))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.totalAulas").value(0))
				.andExpect(jsonPath("$.frequenciaMedia").value(0))
				.andExpect(jsonPath("$.notaMedia").isEmpty())
				.andExpect(jsonPath("$.porAula", hasSize(0)));
	}

	@Test
	void deveRetornar404QuandoTurmaInexistenteAoObterDashboard() throws Exception {
		UUID turmaInexistente = UUID.randomUUID();

		mockMvc.perform(get("/api/turmas/{turmaId}/dashboard", turmaInexistente)
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAdmin))
				.andExpect(status().isNotFound());
	}

	private UUID criarCursoAtivo(String nome) {
		Curso curso = new Curso();
		curso.setNome(nome);
		curso.setAtivo(true);
		return cursoRepository.save(curso).getId();
	}

	private String criarTurma(UUID cursoId, String nome) throws Exception {
		var resultado = mockMvc.perform(post("/api/turmas")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAdmin)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"cursoId": "%s", "nome": "%s"}
						""".formatted(cursoId, nome)))
				.andReturn();
		return objectMapper.readTree(resultado.getResponse().getContentAsString()).get("id").asText();
	}

}
