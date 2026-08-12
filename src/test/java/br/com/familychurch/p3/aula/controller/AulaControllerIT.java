package br.com.familychurch.p3.aula.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.familychurch.p3.IntegrationTestSupport;
import br.com.familychurch.p3.aluno.repository.AlunoRepository;
import br.com.familychurch.p3.aula.repository.AulaRepository;
import br.com.familychurch.p3.avaliacao.repository.AvaliacaoRepository;
import br.com.familychurch.p3.curso.Curso;
import br.com.familychurch.p3.curso.repository.CursoRepository;
import br.com.familychurch.p3.presenca.repository.PresencaRepository;
import br.com.familychurch.p3.turma.StatusTurma;
import br.com.familychurch.p3.turma.Turma;
import br.com.familychurch.p3.turma.repository.TurmaRepository;
import java.time.LocalDate;
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

	@Autowired
	private AvaliacaoRepository avaliacaoRepository;

	private String tokenAdmin;
	private Turma turmaAtiva;

	@BeforeEach
	void configurarCenario() throws Exception {
		avaliacaoRepository.deleteAll();
		presencaRepository.deleteAll();
		aulaRepository.deleteAll();
		alunoRepository.deleteAll();
		turmaRepository.deleteAll();
		tokenAdmin = obterTokenAdmin();
		turmaAtiva = criarTurma(StatusTurma.ATIVA);
	}

	@Test
	void deveDefinirAgendaCriandoUmaAulaPorTipoDeAula() throws Exception {
		UUID tipoAula1 = obterTipoAulaPorNumero(1);
		UUID tipoAula2 = obterTipoAulaPorNumero(2);

		mockMvc.perform(put("/api/turmas/{turmaId}/agenda", turmaAtiva.getId())
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAdmin)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"itens": [
							{"tipoAulaId": "%s", "data": "2026-01-06"},
							{"tipoAulaId": "%s", "data": "2026-01-13"}
						]}
						""".formatted(tipoAula1, tipoAula2)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(2)))
				.andExpect(jsonPath("$[0].numero").value(1))
				.andExpect(jsonPath("$[1].numero").value(2));
	}

	@Test
	void deveMarcarAulaComoAtivaQuandoDataDaAgendaEHoje() throws Exception {
		UUID tipoAula1 = obterTipoAulaPorNumero(1);
		definirAgenda(turmaAtiva.getId(), tipoAula1, LocalDate.now());

		mockMvc.perform(get("/api/publico/turmas/{token}/aula-ativa", turmaAtiva.getTokenCadastro()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.numero").value(1))
				.andExpect(jsonPath("$.status").value("ATIVA"));
	}

	@Test
	void deveRetornar404QuandoNenhumaAulaAtivaHoje() throws Exception {
		UUID tipoAula1 = obterTipoAulaPorNumero(1);
		definirAgenda(turmaAtiva.getId(), tipoAula1, LocalDate.now().plusDays(7));

		mockMvc.perform(get("/api/publico/turmas/{token}/aula-ativa", turmaAtiva.getTokenCadastro()))
				.andExpect(status().isNotFound());
	}

	@Test
	void deveRetornar404QuandoTurmaInexistenteAoDefinirAgenda() throws Exception {
		UUID turmaInexistente = UUID.randomUUID();
		UUID tipoAula1 = obterTipoAulaPorNumero(1);

		mockMvc.perform(put("/api/turmas/{turmaId}/agenda", turmaInexistente)
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAdmin)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"itens": [{"tipoAulaId": "%s", "data": "2026-01-06"}]}
						""".formatted(tipoAula1)))
				.andExpect(status().isNotFound());
	}

	@Test
	void deveRetornar409QuandoTurmaEncerrada() throws Exception {
		Turma turmaEncerrada = criarTurma(StatusTurma.ENCERRADA);
		UUID tipoAula1 = obterTipoAulaPorNumero(1);

		mockMvc.perform(put("/api/turmas/{turmaId}/agenda", turmaEncerrada.getId())
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAdmin)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"itens": [{"tipoAulaId": "%s", "data": "2026-01-06"}]}
						""".formatted(tipoAula1)))
				.andExpect(status().isConflict());
	}

	@Test
	void deveListarAulasDaTurmaOrdenadasPeloCatalogo() throws Exception {
		UUID tipoAula2 = obterTipoAulaPorNumero(2);
		UUID tipoAula1 = obterTipoAulaPorNumero(1);
		definirAgenda(turmaAtiva.getId(), tipoAula2, LocalDate.now().plusDays(14));
		definirAgenda(turmaAtiva.getId(), tipoAula1, LocalDate.now().plusDays(7));

		mockMvc.perform(get("/api/turmas/{turmaId}/aulas", turmaAtiva.getId())
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAdmin))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(2)))
				.andExpect(jsonPath("$[0].numero").value(1))
				.andExpect(jsonPath("$[1].numero").value(2));
	}

	private void definirAgenda(UUID turmaId, UUID tipoAulaId, LocalDate data) throws Exception {
		mockMvc.perform(put("/api/turmas/{turmaId}/agenda", turmaId)
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAdmin)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"itens": [{"tipoAulaId": "%s", "data": "%s"}]}
						""".formatted(tipoAulaId, data)));
	}

	private Turma criarTurma(StatusTurma status) {
		Curso curso = cursoRepository.findById(obterCursoEscolaHombridade()).orElseThrow();

		Turma turma = new Turma();
		turma.setCurso(curso);
		turma.setNome("Campus 85 - Turma 13 - " + System.nanoTime());
		turma.setTokenCadastro("TK" + System.nanoTime() % 1_000_000);
		turma.setStatus(status);
		return turmaRepository.save(turma);
	}

}
