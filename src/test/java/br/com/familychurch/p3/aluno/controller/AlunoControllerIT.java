package br.com.familychurch.p3.aluno.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.familychurch.p3.IntegrationTestSupport;
import br.com.familychurch.p3.aluno.repository.AlunoRepository;
import br.com.familychurch.p3.aula.repository.AulaRepository;
import br.com.familychurch.p3.avaliacao.repository.AvaliacaoRepository;
import br.com.familychurch.p3.curso.Curso;
import br.com.familychurch.p3.curso.repository.CursoRepository;
import br.com.familychurch.p3.historico.repository.HistoricoItemRepository;
import br.com.familychurch.p3.participante.Participante;
import br.com.familychurch.p3.participante.repository.ParticipanteRepository;
import br.com.familychurch.p3.presenca.repository.PresencaRepository;
import br.com.familychurch.p3.turma.StatusTurma;
import br.com.familychurch.p3.turma.Turma;
import br.com.familychurch.p3.turma.repository.TurmaRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Aluno nao tem mais cadastro proprio por formulario (ver
 * PresencaControllerIT para a matricula automatica na primeira chamada) -
 * este IT cobre so a listagem, unico endpoint que resta na feature.
 */
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

	@Autowired
	private AvaliacaoRepository avaliacaoRepository;

	@Autowired
	private ParticipanteRepository participanteRepository;

	@Autowired
	private HistoricoItemRepository historicoItemRepository;

	private Turma turmaAtiva;

	@BeforeEach
	void configurarCenario() throws Exception {
		avaliacaoRepository.deleteAll();
		historicoItemRepository.deleteAll();
		presencaRepository.deleteAll();
		aulaRepository.deleteAll();
		alunoRepository.deleteAll();
		participanteRepository.deleteAll();
		turmaRepository.deleteAll();
		turmaAtiva = criarTurma(StatusTurma.ATIVA);
	}

	@Test
	void deveRetornarNaoAutorizadoAoListarAlunosSemToken() throws Exception {
		mockMvc.perform(get("/api/turmas/{turmaId}/alunos", turmaAtiva.getId()))
				.andExpect(status().is4xxClientError());
	}

	@Test
	void deveListarAlunosDaTurmaComTokenValido() throws Exception {
		cadastrarParticipante(CPF_VALIDO, "Joao Silva");
		mockMvc.perform(put("/api/turmas/{turmaId}/agenda", turmaAtiva.getId())
				.with(autenticado())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"itens": [{"tipoAulaId": "%s", "data": "%s"}]}
						""".formatted(obterTipoAulaPorNumero(1), LocalDate.now())));
		mockMvc.perform(post("/api/publico/turmas/{token}/chamada", turmaAtiva.getTokenCadastro())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"cpf": "%s"}
						""".formatted(CPF_VALIDO)));

		mockMvc.perform(get("/api/turmas/{turmaId}/alunos", turmaAtiva.getId())
				.with(autenticado()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].cpf").value(CPF_VALIDO));
	}

	@Test
	void deveMatricularDiretoComTokenValido() throws Exception {
		String token = cadastrarParticipante(CPF_VALIDO, "Joao Silva");

		mockMvc.perform(post("/api/publico/turmas/{turmaId}/matricula", turmaAtiva.getId())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"cpf": "%s", "token": "%s"}
						""".formatted(CPF_VALIDO, token)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.turmaId").value(turmaAtiva.getId().toString()));
	}

	@Test
	void deveRetornar403AoMatricularComTokenInvalido() throws Exception {
		cadastrarParticipante(CPF_VALIDO, "Joao Silva");

		mockMvc.perform(post("/api/publico/turmas/{turmaId}/matricula", turmaAtiva.getId())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"cpf": "%s", "token": "TOKEN-ERRADO"}
						""".formatted(CPF_VALIDO)))
				.andExpect(status().isForbidden());
	}

	@Test
	void deveRetornar403AoMatricularComTokenExpirado() throws Exception {
		String token = cadastrarParticipante(CPF_VALIDO, "Joao Silva");
		Participante participante = participanteRepository.findByCpf(CPF_VALIDO).orElseThrow();
		participante.setTokenMatriculaExpiraEm(LocalDateTime.now().minusMinutes(1));
		participanteRepository.save(participante);

		mockMvc.perform(post("/api/publico/turmas/{turmaId}/matricula", turmaAtiva.getId())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"cpf": "%s", "token": "%s"}
						""".formatted(CPF_VALIDO, token)))
				.andExpect(status().isForbidden());
	}

	private String cadastrarParticipante(String cpf, String nome) throws Exception {
		var resultado = mockMvc.perform(post("/api/publico/participantes")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{
						  "nome": "%s",
						  "cpf": "%s",
						  "celular": "62999998888",
						  "email": "joao@example.com",
						  "dataNascimento": "1990-05-20",
						  "estadoCivil": "SOLTEIRO",
						  "temFilhos": false,
						  "cep": "74000-000",
						  "bairro": "Centro",
						  "cidade": "Goiania",
						  "uf": "GO",
						  "tempoNaFamily": "MAIS_DE_3_ANOS",
						  "batizado": true,
						  "serveMinisterio": false,
						  "cursosOutros": "Curso de Lideranca Local",
						  "eventosOutros": "Retiro da Familia",
						  "participaGc": false,
						  "aceiteLgpd": true
						}
						""".formatted(nome, cpf)))
				.andReturn();
		return objectMapper.readTree(resultado.getResponse().getContentAsString()).get("tokenMatricula").asText();
	}

	private Turma criarTurma(StatusTurma status) {
		Curso curso = cursoRepository.findById(obterCursoEscolaHombridade()).orElseThrow();

		Turma turma = new Turma();
		turma.setCurso(curso);
		turma.setNome("Campus 85 - Turma " + System.nanoTime());
		turma.setTokenCadastro("TK" + System.nanoTime() % 1_000_000);
		turma.setStatus(status);
		return turmaRepository.save(turma);
	}

}
