package br.com.familychurch.p3.aula.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.familychurch.p3.IntegrationTestSupport;
import br.com.familychurch.p3.aluno.repository.AlunoRepository;
import br.com.familychurch.p3.aula.repository.AulaRepository;
import br.com.familychurch.p3.curso.Curso;
import br.com.familychurch.p3.curso.repository.CursoRepository;
import br.com.familychurch.p3.presenca.repository.PresencaRepository;
import br.com.familychurch.p3.turma.StatusTurma;
import br.com.familychurch.p3.turma.Turma;
import br.com.familychurch.p3.turma.repository.TurmaRepository;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

class AulaControllerIT extends IntegrationTestSupport {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private CursoRepository cursoRepository;

	@Autowired
	private TurmaRepository turmaRepository;

	@Autowired
	private AulaRepository aulaRepository;

	@Autowired
	private AlunoRepository alunoRepository;

	@Autowired
	private PresencaRepository presencaRepository;

	private String tokenAdmin;
	private Turma turmaAtiva;

	@BeforeEach
	void configurarCenario() throws Exception {
		presencaRepository.deleteAll();
		aulaRepository.deleteAll();
		alunoRepository.deleteAll();
		turmaRepository.deleteAll();
		cursoRepository.deleteAll();
		tokenAdmin = obterTokenAdmin();
		turmaAtiva = criarTurma(StatusTurma.ATIVA);
	}

	@Test
	void deveCriarAulaComQrTokenUnico() throws Exception {
		mockMvc.perform(post("/api/turmas/{turmaId}/aulas", turmaAtiva.getId())
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAdmin)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"numero": 1, "modulo": "Provisao", "data": "2026-07-02", "janelaAbertura": "2026-07-02T19:00:00", "janelaFechamento": "2026-07-02T21:00:00"}
						"""))
				.andExpect(status().isCreated())
				.andExpect(header().exists(HttpHeaders.LOCATION))
				.andExpect(jsonPath("$.numero").value(1))
				.andExpect(jsonPath("$.qrToken").isNotEmpty());
	}

	@Test
	void deveRetornar404QuandoTurmaInexistenteAoCriarAula() throws Exception {
		UUID turmaInexistente = UUID.randomUUID();

		mockMvc.perform(post("/api/turmas/{turmaId}/aulas", turmaInexistente)
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAdmin)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"numero": 1, "data": "2026-07-02", "janelaAbertura": "2026-07-02T19:00:00", "janelaFechamento": "2026-07-02T21:00:00"}
						"""))
				.andExpect(status().isNotFound());
	}

	@Test
	void deveRetornar409QuandoTurmaEncerrada() throws Exception {
		Turma turmaEncerrada = criarTurma(StatusTurma.ENCERRADA);

		mockMvc.perform(post("/api/turmas/{turmaId}/aulas", turmaEncerrada.getId())
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAdmin)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"numero": 1, "data": "2026-07-02", "janelaAbertura": "2026-07-02T19:00:00", "janelaFechamento": "2026-07-02T21:00:00"}
						"""))
				.andExpect(status().isConflict());
	}

	@Test
	void deveRetornar400QuandoJanelaFechamentoNaoPosteriorAAbertura() throws Exception {
		mockMvc.perform(post("/api/turmas/{turmaId}/aulas", turmaAtiva.getId())
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAdmin)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"numero": 1, "data": "2026-07-02", "janelaAbertura": "2026-07-02T21:00:00", "janelaFechamento": "2026-07-02T19:00:00"}
						"""))
				.andExpect(status().isBadRequest());
	}

	@Test
	void deveListarAulasDaTurma() throws Exception {
		criarAula(turmaAtiva.getId());

		mockMvc.perform(get("/api/turmas/{turmaId}/aulas", turmaAtiva.getId())
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAdmin))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)));
	}

	@Test
	void deveGerarImagemPngDoQrCode() throws Exception {
		String aulaId = criarAula(turmaAtiva.getId());

		mockMvc.perform(get("/api/aulas/{id}/qrcode", aulaId)
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAdmin))
				.andExpect(status().isOk())
				.andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaType.IMAGE_PNG_VALUE));
	}

	private String criarAula(UUID turmaId) throws Exception {
		var resultado = mockMvc.perform(post("/api/turmas/{turmaId}/aulas", turmaId)
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAdmin)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"numero": 1, "data": "2026-07-02", "janelaAbertura": "2026-07-02T19:00:00", "janelaFechamento": "2026-07-02T21:00:00"}
						"""))
				.andReturn();
		return objectMapper.readTree(resultado.getResponse().getContentAsString()).get("id").asText();
	}

	private Turma criarTurma(StatusTurma status) {
		Curso curso = new Curso();
		curso.setNome("Escola de Hombridade " + status + System.nanoTime());
		curso.setAtivo(true);
		cursoRepository.save(curso);

		Turma turma = new Turma();
		turma.setCurso(curso);
		turma.setNome("Campus 85 - Turma 13");
		turma.setTokenCadastro("TK" + System.nanoTime() % 1_000_000);
		turma.setStatus(status);
		return turmaRepository.save(turma);
	}

}
