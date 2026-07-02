package br.com.familychurch.p3.presenca.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.familychurch.p3.IntegrationTestSupport;
import br.com.familychurch.p3.aluno.repository.AlunoRepository;
import br.com.familychurch.p3.aula.repository.AulaRepository;
import br.com.familychurch.p3.avaliacao.repository.AvaliacaoRepository;
import br.com.familychurch.p3.curso.Curso;
import br.com.familychurch.p3.curso.repository.CursoRepository;
import br.com.familychurch.p3.interesse.repository.InteresseRepository;
import br.com.familychurch.p3.presenca.repository.PresencaRepository;
import br.com.familychurch.p3.turma.StatusTurma;
import br.com.familychurch.p3.turma.Turma;
import br.com.familychurch.p3.turma.repository.TurmaRepository;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

class PresencaControllerIT extends IntegrationTestSupport {

	private static final String CPF_ALUNO_TURMA = "52998224725";
	private static final String CPF_ALUNO_OUTRA_TURMA = "11144477735";

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

	@Autowired
	private InteresseRepository interesseRepository;

	private String tokenAdmin;
	private Turma turmaAtiva;

	@BeforeEach
	void configurarCenario() throws Exception {
		avaliacaoRepository.deleteAll();
		interesseRepository.deleteAll();
		presencaRepository.deleteAll();
		aulaRepository.deleteAll();
		alunoRepository.deleteAll();
		turmaRepository.deleteAll();
		cursoRepository.deleteAll();
		tokenAdmin = obterTokenAdmin();
		turmaAtiva = criarTurma(StatusTurma.ATIVA);
		cadastrarAluno(turmaAtiva.getTokenCadastro(), CPF_ALUNO_TURMA);
	}

	@Test
	void deveRegistrarCheckinComQrTokenEcpfValidos() throws Exception {
		String qrToken = criarAulaComJanelaAberta();

		mockMvc.perform(post("/api/publico/checkin")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"qrToken": "%s", "cpf": "%s"}
						""".formatted(qrToken, CPF_ALUNO_TURMA)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.metodo").value("QRCODE"));
	}

	@Test
	void deveRetornar404QuandoQrTokenInvalido() throws Exception {
		mockMvc.perform(post("/api/publico/checkin")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"qrToken": "TOKEN-INEXISTENTE", "cpf": "%s"}
						""".formatted(CPF_ALUNO_TURMA)))
				.andExpect(status().isNotFound());
	}

	@Test
	void deveRetornar400QuandoJanelaDaAulaFechada() throws Exception {
		String qrToken = criarAulaComJanela(
				LocalDateTime.now().minusHours(3), LocalDateTime.now().minusHours(1));

		mockMvc.perform(post("/api/publico/checkin")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"qrToken": "%s", "cpf": "%s"}
						""".formatted(qrToken, CPF_ALUNO_TURMA)))
				.andExpect(status().isBadRequest());
	}

	@Test
	void deveRetornar404QuandoCpfNaoCadastrado() throws Exception {
		String qrToken = criarAulaComJanelaAberta();

		mockMvc.perform(post("/api/publico/checkin")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"qrToken": "%s", "cpf": "12345678909"}
						""".formatted(qrToken)))
				.andExpect(status().isNotFound());
	}

	@Test
	void deveRetornar400QuandoAlunoNaoPertenceATurmaDaAula() throws Exception {
		Turma outraTurma = criarTurma(StatusTurma.ATIVA);
		cadastrarAluno(outraTurma.getTokenCadastro(), CPF_ALUNO_OUTRA_TURMA);
		String qrToken = criarAulaComJanelaAberta();

		mockMvc.perform(post("/api/publico/checkin")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"qrToken": "%s", "cpf": "%s"}
						""".formatted(qrToken, CPF_ALUNO_OUTRA_TURMA)))
				.andExpect(status().isBadRequest());
	}

	@Test
	void deveRetornar409QuandoCheckinDuplicado() throws Exception {
		String qrToken = criarAulaComJanelaAberta();
		realizarCheckin(qrToken, CPF_ALUNO_TURMA);

		mockMvc.perform(post("/api/publico/checkin")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"qrToken": "%s", "cpf": "%s"}
						""".formatted(qrToken, CPF_ALUNO_TURMA)))
				.andExpect(status().isConflict());
	}

	@Test
	void deveMarcarPresencaManualComoFallback() throws Exception {
		String aulaId = criarAula(LocalDateTime.now().minusHours(3), LocalDateTime.now().minusHours(1));

		mockMvc.perform(post("/api/aulas/{aulaId}/presencas/manual", aulaId)
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAdmin)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"cpf": "%s"}
						""".formatted(CPF_ALUNO_TURMA)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.metodo").value("MANUAL"))
				.andExpect(jsonPath("$.validadoPorNome").isNotEmpty());
	}

	@Test
	void deveListarPresencasDeUmaAula() throws Exception {
		String qrToken = criarAulaComJanelaAberta();
		realizarCheckin(qrToken, CPF_ALUNO_TURMA);
		String aulaId = aulaRepository.findByQrToken(qrToken).orElseThrow().getId().toString();

		mockMvc.perform(get("/api/aulas/{aulaId}/presencas", aulaId)
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAdmin))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].alunoNome").value("Joao Silva"));
	}

	private void realizarCheckin(String qrToken, String cpf) throws Exception {
		mockMvc.perform(post("/api/publico/checkin")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"qrToken": "%s", "cpf": "%s"}
						""".formatted(qrToken, cpf)));
	}

	private String criarAulaComJanelaAberta() throws Exception {
		return criarAulaEObterQrToken(LocalDateTime.now().minusHours(1), LocalDateTime.now().plusHours(1));
	}

	private String criarAulaComJanela(LocalDateTime abertura, LocalDateTime fechamento) throws Exception {
		return criarAulaEObterQrToken(abertura, fechamento);
	}

	private String criarAulaEObterQrToken(LocalDateTime abertura, LocalDateTime fechamento) throws Exception {
		String aulaId = criarAula(abertura, fechamento);
		return aulaRepository.findById(java.util.UUID.fromString(aulaId)).orElseThrow().getQrToken();
	}

	private String criarAula(LocalDateTime abertura, LocalDateTime fechamento) throws Exception {
		DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
		var resultado = mockMvc.perform(post("/api/turmas/{turmaId}/aulas", turmaAtiva.getId())
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAdmin)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"numero": 1, "data": "2026-07-02", "janelaAbertura": "%s", "janelaFechamento": "%s"}
						""".formatted(abertura.format(formatter), fechamento.format(formatter))))
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
