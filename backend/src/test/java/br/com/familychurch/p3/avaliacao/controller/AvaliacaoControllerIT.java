package br.com.familychurch.p3.avaliacao.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.familychurch.p3.IntegrationTestSupport;
import br.com.familychurch.p3.aluno.repository.AlunoRepository;
import br.com.familychurch.p3.aula.Aula;
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

class AvaliacaoControllerIT extends IntegrationTestSupport {

	private static final String CPF_COM_PRESENCA = "52998224725";
	private static final String CPF_SEM_PRESENCA = "11144477735";

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
		cadastrarParticipante(CPF_COM_PRESENCA, "Joao Silva");
		cadastrarParticipante(CPF_SEM_PRESENCA, "Pedro Alves");
	}

	@Test
	void deveRegistrarAvaliacaoQuandoParticipanteJaTemPresenca() throws Exception {
		String aulaId = criarAulaAtiva(1);
		registrarChamada(turmaAtiva.getTokenCadastro(), CPF_COM_PRESENCA);

		mockMvc.perform(post("/api/publico/aulas/{aulaId}/avaliacoes", aulaId)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"cpf": "%s", "nota": 9, "didatica": "OTIMA", "sugestao": "Otima aula", "nps": 10}
						""".formatted(CPF_COM_PRESENCA)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.nota").value(9))
				.andExpect(jsonPath("$.didatica").value("OTIMA"))
				.andExpect(jsonPath("$.nps").value(10));
	}

	@Test
	void deveRetornar404QuandoParticipanteAindaNaoRegistrouPresenca() throws Exception {
		String aulaId = criarAulaAtiva(1);

		mockMvc.perform(post("/api/publico/aulas/{aulaId}/avaliacoes", aulaId)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"cpf": "%s", "nota": 9, "didatica": "OTIMA", "nps": 10}
						""".formatted(CPF_SEM_PRESENCA)))
				.andExpect(status().isNotFound());
	}

	@Test
	void deveRetornar409QuandoAvaliacaoDuplicada() throws Exception {
		String aulaId = criarAulaAtiva(1);
		registrarChamada(turmaAtiva.getTokenCadastro(), CPF_COM_PRESENCA);
		registrarAvaliacao(aulaId, CPF_COM_PRESENCA);

		mockMvc.perform(post("/api/publico/aulas/{aulaId}/avaliacoes", aulaId)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"cpf": "%s", "nota": 8, "didatica": "BOA", "nps": 9}
						""".formatted(CPF_COM_PRESENCA)))
				.andExpect(status().isConflict());
	}

	@Test
	void deveRetornar400QuandoNotaForaDoIntervalo() throws Exception {
		String aulaId = criarAulaAtiva(1);
		registrarChamada(turmaAtiva.getTokenCadastro(), CPF_COM_PRESENCA);

		mockMvc.perform(post("/api/publico/aulas/{aulaId}/avaliacoes", aulaId)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"cpf": "%s", "nota": 11, "didatica": "OTIMA", "nps": 10}
						""".formatted(CPF_COM_PRESENCA)))
				.andExpect(status().isBadRequest());
	}

	@Test
	void deveGravarDesejaServirEHabilidadesNaAula03() throws Exception {
		String aulaId = criarAulaAtiva(3);
		registrarChamada(turmaAtiva.getTokenCadastro(), CPF_COM_PRESENCA);

		mockMvc.perform(post("/api/publico/aulas/{aulaId}/avaliacoes", aulaId)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"cpf": "%s", "nota": 9, "didatica": "OTIMA", "nps": 10, "desejaServir": true, "habilidades": "Som e midia"}
						""".formatted(CPF_COM_PRESENCA)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.desejaServir").value("Sim"))
				.andExpect(jsonPath("$.habilidades").value("Som e midia"));

		var participante = participanteRepository.findByCpf(CPF_COM_PRESENCA).orElseThrow();
		org.assertj.core.api.Assertions.assertThat(participante.getDesejaServir()).isEqualTo("Sim");
		org.assertj.core.api.Assertions.assertThat(participante.getHabilidades()).isEqualTo("Som e midia");
	}

	@Test
	void deveListarAvaliacoesDeUmaAula() throws Exception {
		String aulaId = criarAulaAtiva(1);
		registrarChamada(turmaAtiva.getTokenCadastro(), CPF_COM_PRESENCA);
		registrarAvaliacao(aulaId, CPF_COM_PRESENCA);

		mockMvc.perform(get("/api/aulas/{aulaId}/avaliacoes", aulaId)
				.with(autenticado()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].participanteNome").value("Joao Silva"));
	}

	private void registrarAvaliacao(String aulaId, String cpf) throws Exception {
		mockMvc.perform(post("/api/publico/aulas/{aulaId}/avaliacoes", aulaId)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"cpf": "%s", "nota": 9, "didatica": "OTIMA", "nps": 10}
						""".formatted(cpf)));
	}

	private void registrarChamada(String token, String cpf) throws Exception {
		mockMvc.perform(post("/api/publico/turmas/{token}/chamada", token)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"cpf": "%s"}
						""".formatted(cpf)));
	}

	private String criarAulaAtiva(int numeroTipoAula) throws Exception {
		java.util.UUID tipoAulaId = obterTipoAulaPorNumero(numeroTipoAula);
		mockMvc.perform(put("/api/turmas/{turmaId}/agenda", turmaAtiva.getId())
				.with(autenticado())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"itens": [{"tipoAulaId": "%s", "data": "%s"}]}
						""".formatted(tipoAulaId, LocalDate.now())));
		Aula aula = aulaRepository.findByTurmaIdAndTipoAulaId(turmaAtiva.getId(), tipoAulaId).orElseThrow();
		return aula.getId().toString();
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
