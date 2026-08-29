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
import br.com.familychurch.p3.usuarioadmin.Papel;
import br.com.familychurch.p3.usuarioadmin.UsuarioAdmin;
import br.com.familychurch.p3.usuarioadmin.repository.UsuarioAdminRepository;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
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

	@Autowired
	private UsuarioAdminRepository usuarioAdminRepository;

	@BeforeEach
	void configurarCenario() {
		avaliacaoRepository.deleteAll();
		presencaRepository.deleteAll();
		aulaRepository.deleteAll();
		alunoRepository.deleteAll();
		turmaRepository.deleteAll();
	}

	@Test
	void deveCriarTurmaVinculadaACursoAtivoComTokenUnico() throws Exception {
		UUID cursoId = obterCursoEscolaHombridade();

		mockMvc.perform(post("/api/turmas")
				.with(autenticado())
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
				.with(autenticado())
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
				.with(autenticado()));

		mockMvc.perform(post("/api/turmas")
				.with(autenticado())
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
				.with(autenticado()));

		mockMvc.perform(get("/api/turmas").param("status", "ATIVA")
				.with(autenticado()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].nome").value("Turma ativa"));
	}

	@Test
	void deveEncerrarTurma() throws Exception {
		UUID cursoId = obterCursoEscolaHombridade();
		String turmaId = criarTurma(cursoId, "Turma a encerrar");

		mockMvc.perform(patch("/api/turmas/{id}/encerrar", turmaId)
				.with(autenticado()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("ENCERRADA"));
	}

	@Test
	void deveObterDashboardDeTurmaSemAulas() throws Exception {
		UUID cursoId = obterCursoEscolaHombridade();
		String turmaId = criarTurma(cursoId, "Turma sem aulas");

		mockMvc.perform(get("/api/turmas/{turmaId}/dashboard", turmaId)
				.with(autenticado()))
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
				.with(autenticado()))
				.andExpect(status().isNotFound());
	}

	@Test
	void deveCriarTurmaComLiderVinculado() throws Exception {
		UUID cursoId = obterCursoEscolaHombridade();
		UUID liderId = criarLider("Paulo", UUID.randomUUID());

		mockMvc.perform(post("/api/turmas")
				.with(autenticado())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"cursoId": "%s", "nome": "Turma do Paulo", "liderId": "%s"}
						""".formatted(cursoId, liderId)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.liderId").value(liderId.toString()))
				.andExpect(jsonPath("$.liderNome").value("Paulo"));
	}

	@Test
	void deveRetornar404QuandoLiderInexistenteAoCriarTurma() throws Exception {
		UUID cursoId = obterCursoEscolaHombridade();
		UUID liderInexistente = UUID.randomUUID();

		mockMvc.perform(post("/api/turmas")
				.with(autenticado())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"cursoId": "%s", "nome": "Turma sem lider valido", "liderId": "%s"}
						""".formatted(cursoId, liderInexistente)))
				.andExpect(status().isNotFound());
	}

	@Test
	void liderRestritoSoVeSuaPropriaTurmaNaListagem() throws Exception {
		UUID cursoId = obterCursoEscolaHombridade();
		UUID pessoaPaulo = UUID.randomUUID();
		UUID pessoaMarcio = UUID.randomUUID();
		UUID liderPaulo = criarLider("Paulo", pessoaPaulo);
		UUID liderMarcio = criarLider("Marcio", pessoaMarcio);
		criarTurmaComLider(cursoId, "Turma do Paulo", liderPaulo);
		criarTurmaComLider(cursoId, "Turma do Marcio", liderMarcio);

		mockMvc.perform(get("/api/turmas")
				.with(autenticadoComoLiderComEscopo(pessoaPaulo, "85")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].nome").value("Turma do Paulo"));
	}

	@Test
	void liderRestritoRecebe403AoBuscarTurmaDeOutroLider() throws Exception {
		UUID cursoId = obterCursoEscolaHombridade();
		UUID pessoaPaulo = UUID.randomUUID();
		UUID pessoaMarcio = UUID.randomUUID();
		UUID liderMarcio = criarLider("Marcio", pessoaMarcio);
		String turmaDoMarcio = criarTurmaComLider(cursoId, "Turma do Marcio", liderMarcio);

		mockMvc.perform(get("/api/turmas/{id}", turmaDoMarcio)
				.with(autenticadoComoLiderComEscopo(pessoaPaulo, "85")))
				.andExpect(status().isForbidden());
	}

	@Test
	void liderSemEscopoVeTodasAsTurmas() throws Exception {
		UUID cursoId = obterCursoEscolaHombridade();
		UUID liderPaulo = criarLider("Paulo", UUID.randomUUID());
		criarTurmaComLider(cursoId, "Turma do Paulo", liderPaulo);
		criarTurma(cursoId, "Turma sem lider");

		mockMvc.perform(get("/api/turmas")
				.with(autenticadoComoLiderSemEscopo(UUID.randomUUID())))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(2)));
	}

	private UUID criarLider(String nome, UUID pessoaId) {
		UsuarioAdmin lider = new UsuarioAdmin();
		lider.setNome(nome);
		lider.setEmail(nome.toLowerCase() + "-" + UUID.randomUUID() + "@hombridade.local");
		lider.setSenha("senha-nao-usada");
		lider.setPapel(Papel.LIDER);
		lider.setPessoaId(pessoaId);
		return usuarioAdminRepository.save(lider).getId();
	}

	private String criarTurmaComLider(UUID cursoId, String nome, UUID liderId) throws Exception {
		var resultado = mockMvc.perform(post("/api/turmas")
				.with(autenticado())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"cursoId": "%s", "nome": "%s", "liderId": "%s"}
						""".formatted(cursoId, nome, liderId)))
				.andReturn();
		return objectMapper.readTree(resultado.getResponse().getContentAsString()).get("id").asText();
	}

	private UUID criarCursoAtivo(String nome) {
		Curso curso = new Curso();
		curso.setNome(nome);
		curso.setAtivo(true);
		return cursoRepository.save(curso).getId();
	}

	private String criarTurma(UUID cursoId, String nome) throws Exception {
		var resultado = mockMvc.perform(post("/api/turmas")
				.with(autenticado())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"cursoId": "%s", "nome": "%s"}
						""".formatted(cursoId, nome)))
				.andReturn();
		return objectMapper.readTree(resultado.getResponse().getContentAsString()).get("id").asText();
	}

}
