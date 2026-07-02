package br.com.familychurch.p3.aluno.controller;

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
import br.com.familychurch.p3.presenca.repository.PresencaRepository;
import br.com.familychurch.p3.turma.StatusTurma;
import br.com.familychurch.p3.turma.Turma;
import br.com.familychurch.p3.turma.repository.TurmaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

class AlunoControllerIT extends IntegrationTestSupport {

	private static final String CPF_VALIDO = "52998224725";

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
	void deveCadastrarAlunoComCpfValidoEAceiteLgpd() throws Exception {
		mockMvc.perform(post("/api/publico/turmas/{token}/alunos", turmaAtiva.getTokenCadastro())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"nome": "Joao Silva", "cpf": "%s", "celular": "11999999999", "campus": "Campus 85", "aceiteLgpd": true}
						""".formatted(CPF_VALIDO)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.nome").value("Joao Silva"));
	}

	@Test
	void deveRetornar400QuandoCpfComDigitoVerificadorInvalido() throws Exception {
		mockMvc.perform(post("/api/publico/turmas/{token}/alunos", turmaAtiva.getTokenCadastro())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"nome": "Joao Silva", "cpf": "52998224700", "celular": "11999999999", "campus": "Campus 85", "aceiteLgpd": true}
						"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.erros[0].campo").value("cpf"));
	}

	@Test
	void deveRetornar409QuandoCpfJaCadastrado() throws Exception {
		cadastrarAluno(turmaAtiva.getTokenCadastro(), CPF_VALIDO);

		mockMvc.perform(post("/api/publico/turmas/{token}/alunos", turmaAtiva.getTokenCadastro())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"nome": "Outro Aluno", "cpf": "%s", "celular": "11999999999", "campus": "Campus 85", "aceiteLgpd": true}
						""".formatted(CPF_VALIDO)))
				.andExpect(status().isConflict());
	}

	@Test
	void deveRetornar400QuandoAceiteLgpdFalse() throws Exception {
		mockMvc.perform(post("/api/publico/turmas/{token}/alunos", turmaAtiva.getTokenCadastro())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"nome": "Joao Silva", "cpf": "%s", "celular": "11999999999", "campus": "Campus 85", "aceiteLgpd": false}
						""".formatted(CPF_VALIDO)))
				.andExpect(status().isBadRequest());
	}

	@Test
	void deveRetornar409QuandoTurmaEncerrada() throws Exception {
		Turma turmaEncerrada = criarTurma(StatusTurma.ENCERRADA);

		mockMvc.perform(post("/api/publico/turmas/{token}/alunos", turmaEncerrada.getTokenCadastro())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"nome": "Joao Silva", "cpf": "%s", "celular": "11999999999", "campus": "Campus 85", "aceiteLgpd": true}
						""".formatted(CPF_VALIDO)))
				.andExpect(status().isConflict());
	}

	@Test
	void deveRetornarNaoAutorizadoAoListarAlunosSemToken() throws Exception {
		mockMvc.perform(get("/api/turmas/{turmaId}/alunos", turmaAtiva.getId()))
				.andExpect(status().is4xxClientError());
	}

	@Test
	void deveListarAlunosDaTurmaComTokenValido() throws Exception {
		cadastrarAluno(turmaAtiva.getTokenCadastro(), CPF_VALIDO);

		mockMvc.perform(get("/api/turmas/{turmaId}/alunos", turmaAtiva.getId())
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAdmin))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].cpf").value(CPF_VALIDO));
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
