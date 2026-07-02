package br.com.familychurch.p3.interesse.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.familychurch.p3.IntegrationTestSupport;
import br.com.familychurch.p3.aluno.repository.AlunoRepository;
import br.com.familychurch.p3.aula.repository.AulaRepository;
import br.com.familychurch.p3.curso.Curso;
import br.com.familychurch.p3.curso.repository.CursoRepository;
import br.com.familychurch.p3.interesse.repository.InteresseRepository;
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

class InteresseControllerIT extends IntegrationTestSupport {

	private static final String CPF_ALUNO_TURMA = "52998224725";
	private static final String CPF_ALUNO_SEM_CHECKIN = "11144477735";

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
	private InteresseRepository interesseRepository;

	private String tokenAdmin;
	private Turma turmaAtiva;

	@BeforeEach
	void configurarCenario() throws Exception {
		interesseRepository.deleteAll();
		presencaRepository.deleteAll();
		aulaRepository.deleteAll();
		alunoRepository.deleteAll();
		turmaRepository.deleteAll();
		cursoRepository.deleteAll();
		tokenAdmin = obterTokenAdmin();
		turmaAtiva = criarTurma(StatusTurma.ATIVA);
		cadastrarAluno(turmaAtiva.getTokenCadastro(), CPF_ALUNO_TURMA);
		cadastrarAluno(turmaAtiva.getTokenCadastro(), CPF_ALUNO_SEM_CHECKIN);
	}

	@Test
	void deveRegistrarInteresseQuandoAlunoJaFezCheckin() throws Exception {
		String aulaId = criarAula();
		realizarCheckin(aulaId, CPF_ALUNO_TURMA);

		mockMvc.perform(post("/api/publico/aulas/{aulaId}/interesses", aulaId)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"cpf": "%s", "gcHomens": true, "proximoAwake": false, "discipulado": true}
						""".formatted(CPF_ALUNO_TURMA)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.gcHomens").value(true))
				.andExpect(jsonPath("$.proximoAwake").value(false))
				.andExpect(jsonPath("$.discipulado").value(true));
	}

	@Test
	void deveRetornar404QuandoAlunoAindaNaoFezCheckin() throws Exception {
		String aulaId = criarAula();

		mockMvc.perform(post("/api/publico/aulas/{aulaId}/interesses", aulaId)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"cpf": "%s", "gcHomens": true, "proximoAwake": false, "discipulado": false}
						""".formatted(CPF_ALUNO_SEM_CHECKIN)))
				.andExpect(status().isNotFound());
	}

	@Test
	void deveRetornar409QuandoInteresseDuplicado() throws Exception {
		String aulaId = criarAula();
		realizarCheckin(aulaId, CPF_ALUNO_TURMA);
		registrarInteresse(aulaId, CPF_ALUNO_TURMA);

		mockMvc.perform(post("/api/publico/aulas/{aulaId}/interesses", aulaId)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"cpf": "%s", "gcHomens": false, "proximoAwake": true, "discipulado": false}
						""".formatted(CPF_ALUNO_TURMA)))
				.andExpect(status().isConflict());
	}

	@Test
	void deveListarInteressesDaTurma() throws Exception {
		String aulaId = criarAula();
		realizarCheckin(aulaId, CPF_ALUNO_TURMA);
		registrarInteresse(aulaId, CPF_ALUNO_TURMA);

		mockMvc.perform(get("/api/turmas/{turmaId}/interesses", turmaAtiva.getId())
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAdmin))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].alunoNome").value("Joao Silva"));
	}

	private void registrarInteresse(String aulaId, String cpf) throws Exception {
		mockMvc.perform(post("/api/publico/aulas/{aulaId}/interesses", aulaId)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"cpf": "%s", "gcHomens": true, "proximoAwake": false, "discipulado": true}
						""".formatted(cpf)));
	}

	private void realizarCheckin(String aulaId, String cpf) throws Exception {
		String qrToken = aulaRepository.findById(UUID.fromString(aulaId)).orElseThrow().getQrToken();
		mockMvc.perform(post("/api/publico/checkin")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"qrToken": "%s", "cpf": "%s"}
						""".formatted(qrToken, cpf)));
	}

	private String criarAula() throws Exception {
		var resultado = mockMvc.perform(post("/api/turmas/{turmaId}/aulas", turmaAtiva.getId())
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAdmin)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"numero": 1, "data": "2026-07-02", "janelaAbertura": "2026-07-02T00:00:00", "janelaFechamento": "2099-07-02T23:59:00"}
						"""))
				.andReturn();
		return objectMapper.readTree(resultado.getResponse().getContentAsString()).get("id").asText();
	}

	private void cadastrarAluno(String tokenCadastroTurma, String cpf) throws Exception {
		mockMvc.perform(post("/api/publico/turmas/{token}/alunos", tokenCadastroTurma)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"nome": "Joao Silva", "cpf": "%s", "celular": "11999999999", "campus": "Campus 85", "aceiteLgpd": true}
						""".formatted(cpf)));
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
