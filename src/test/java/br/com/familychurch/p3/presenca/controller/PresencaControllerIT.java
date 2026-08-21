package br.com.familychurch.p3.presenca.controller;

import static org.assertj.core.api.Assertions.assertThat;
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
import br.com.familychurch.p3.participante.repository.ParticipanteRepository;
import br.com.familychurch.p3.presenca.repository.PresencaRepository;
import br.com.familychurch.p3.turma.StatusTurma;
import br.com.familychurch.p3.turma.Turma;
import br.com.familychurch.p3.turma.repository.TurmaRepository;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

class PresencaControllerIT extends IntegrationTestSupport {

	private static final String CPF_PARTICIPANTE = "52998224725";
	private static final String CPF_NAO_CADASTRADO = "11144477735";

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

	@BeforeEach
	void configurarCenario() {
		historicoItemRepository.deleteAll();
		avaliacaoRepository.deleteAll();
		presencaRepository.deleteAll();
		aulaRepository.deleteAll();
		alunoRepository.deleteAll();
		participanteRepository.deleteAll();
		turmaRepository.deleteAll();
	}

	@Test
	void deveRegistrarChamadaQuandoParticipanteCadastradoEAulaAtiva() throws Exception {
		cadastrarParticipante(CPF_PARTICIPANTE, "Joao Silva");
		Turma turma = criarTurmaComAulaAtivaHoje(1);

		mockMvc.perform(post("/api/publico/turmas/{token}/chamada", turma.getTokenCadastro())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"cpf": "%s"}
						""".formatted(CPF_PARTICIPANTE)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.participanteNome").value("Joao Silva"))
				.andExpect(jsonPath("$.metodo").value("FORMULARIO"))
				.andExpect(jsonPath("$.origem").value("CHAMADA"));
	}

	@Test
	void deveRetornar404QuandoCpfNaoCadastrado() throws Exception {
		Turma turma = criarTurmaComAulaAtivaHoje(1);

		mockMvc.perform(post("/api/publico/turmas/{token}/chamada", turma.getTokenCadastro())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"cpf": "%s"}
						""".formatted(CPF_NAO_CADASTRADO)))
				.andExpect(status().isNotFound());
	}

	@Test
	void deveRetornar404QuandoNenhumaAulaAtivaNaTurma() throws Exception {
		cadastrarParticipante(CPF_PARTICIPANTE, "Joao Silva");
		Turma turma = criarTurma(StatusTurma.ATIVA);
		definirAgenda(turma.getId(), obterTipoAulaPorNumero(1), LocalDate.now().plusDays(7));

		mockMvc.perform(post("/api/publico/turmas/{token}/chamada", turma.getTokenCadastro())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"cpf": "%s"}
						""".formatted(CPF_PARTICIPANTE)))
				.andExpect(status().isNotFound());
	}

	@Test
	void deveSerIdempotenteAoRegistrarChamadaDuasVezesNaMesmaAula() throws Exception {
		cadastrarParticipante(CPF_PARTICIPANTE, "Joao Silva");
		Turma turma = criarTurmaComAulaAtivaHoje(1);
		registrarChamada(turma.getTokenCadastro(), CPF_PARTICIPANTE);

		mockMvc.perform(post("/api/publico/turmas/{token}/chamada", turma.getTokenCadastro())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"cpf": "%s"}
						""".formatted(CPF_PARTICIPANTE)))
				.andExpect(status().isCreated());

		assertThat(presencaRepository.count()).isEqualTo(1);
	}

	@Test
	void deveClassificarComoReposicaoQuandoChamadaEmTurmaDiferenteDaMatricula() throws Exception {
		cadastrarParticipante(CPF_PARTICIPANTE, "Joao Silva");
		Turma turmaOrigem = criarTurmaComAulaAtivaHoje(1);
		registrarChamada(turmaOrigem.getTokenCadastro(), CPF_PARTICIPANTE);

		Turma outraTurma = criarTurmaComAulaAtivaHoje(2);

		mockMvc.perform(post("/api/publico/turmas/{token}/chamada", outraTurma.getTokenCadastro())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"cpf": "%s"}
						""".formatted(CPF_PARTICIPANTE)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.origem").value("REPOSICAO"));

		assertThat(alunoRepository.count()).isEqualTo(1);
	}

	@Test
	void deveGravarConclusaoNoHistoricoQuandoTodosOsModulosFicamAptos() throws Exception {
		cadastrarParticipante(CPF_PARTICIPANTE, "Joao Silva");
		Turma turma = criarTurma(StatusTurma.ATIVA);

		// Agenda com datas passadas distintas (uma por semana) e marcacao
		// manual do lider - evita depender de "aula ativa hoje" (so uma
		// sessao pode estar ATIVA por vez) para simular as 9 aulas do curso
		// completas de uma vez.
		for (int numero = 1; numero <= 9; numero++) {
			definirAgenda(turma.getId(), obterTipoAulaPorNumero(numero), LocalDate.now().minusWeeks(9 - numero));
			String aulaId = aulaRepository.findByTurmaIdAndTipoAulaId(turma.getId(), obterTipoAulaPorNumero(numero))
					.orElseThrow().getId().toString();
			marcarPresencaManual(aulaId, CPF_PARTICIPANTE);
		}

		var participanteId = participanteRepository.findByCpf(CPF_PARTICIPANTE).orElseThrow().getId();
		var historico = historicoItemRepository.findByParticipanteId(participanteId);
		assertThat(historico).anySatisfy(item -> {
			assertThat(item.getItem()).isEqualTo("Escola de Hombridade");
			assertThat(item.getStatus().name()).isEqualTo("CONCLUIDO");
			assertThat(item.getOrigem().name()).isEqualTo("CHAMADA");
		});
	}

	@Test
	void deveMarcarPresencaManualComoFallback() throws Exception {
		cadastrarParticipante(CPF_PARTICIPANTE, "Joao Silva");
		Turma turma = criarTurmaComAulaAtivaHoje(1);
		String aulaId = aulaRepository.findByTurmaId(turma.getId()).get(0).getId().toString();

		mockMvc.perform(post("/api/aulas/{aulaId}/presencas/manual", aulaId)
				.with(autenticado())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"cpf": "%s"}
						""".formatted(CPF_PARTICIPANTE)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.metodo").value("MANUAL"))
				.andExpect(jsonPath("$.validadoPorNome").isNotEmpty());
	}

	@Test
	void deveRetornar403QuandoValidadorNaoTemUsuarioAdminVinculado() throws Exception {
		cadastrarParticipante(CPF_PARTICIPANTE, "Joao Silva");
		Turma turma = criarTurmaComAulaAtivaHoje(1);
		String aulaId = aulaRepository.findByTurmaId(turma.getId()).get(0).getId().toString();

		mockMvc.perform(post("/api/aulas/{aulaId}/presencas/manual", aulaId)
				.with(autenticadoSemVinculo())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"cpf": "%s"}
						""".formatted(CPF_PARTICIPANTE)))
				.andExpect(status().isForbidden());

		assertThat(presencaRepository.count()).isEqualTo(0);
	}

	private void marcarPresencaManual(String aulaId, String cpf) throws Exception {
		mockMvc.perform(post("/api/aulas/{aulaId}/presencas/manual", aulaId)
				.with(autenticado())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"cpf": "%s"}
						""".formatted(cpf)))
				.andExpect(status().isCreated());
	}

	private void registrarChamada(String token, String cpf) throws Exception {
		mockMvc.perform(post("/api/publico/turmas/{token}/chamada", token)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"cpf": "%s"}
						""".formatted(cpf)));
	}

	private void definirAgenda(java.util.UUID turmaId, java.util.UUID tipoAulaId, LocalDate data) throws Exception {
		mockMvc.perform(put("/api/turmas/{turmaId}/agenda", turmaId)
				.with(autenticado())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"itens": [{"tipoAulaId": "%s", "data": "%s"}]}
						""".formatted(tipoAulaId, data)));
	}

	private Turma criarTurmaComAulaAtivaHoje(int numeroTipoAula) throws Exception {
		Turma turma = criarTurma(StatusTurma.ATIVA);
		definirAgenda(turma.getId(), obterTipoAulaPorNumero(numeroTipoAula), LocalDate.now());
		return turma;
	}

	private void cadastrarParticipante(String cpf, String nome) throws Exception {
		mockMvc.perform(post("/api/publico/participantes")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{
						  "nome": "%s",
						  "cpf": "%s",
						  "celular": "62999998888",
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
						""".formatted(nome, cpf)));
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
