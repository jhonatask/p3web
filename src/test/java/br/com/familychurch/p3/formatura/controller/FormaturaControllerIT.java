package br.com.familychurch.p3.formatura.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

class FormaturaControllerIT extends IntegrationTestSupport {

	private static final String CPF_COMPLETO = "52998224725";
	private static final String CPF_COM_FALTA = "11144477735";

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

	private Turma turma;

	@BeforeEach
	void configurarCenario() throws Exception {
		historicoItemRepository.deleteAll();
		avaliacaoRepository.deleteAll();
		presencaRepository.deleteAll();
		aulaRepository.deleteAll();
		alunoRepository.deleteAll();
		participanteRepository.deleteAll();
		turmaRepository.deleteAll();
		turma = criarTurma(StatusTurma.ATIVA);
	}

	@Test
	void devePreencherPainelComStatusPorModuloEReposicaoPendente() throws Exception {
		cadastrarParticipante(CPF_COM_FALTA, "Pedro Alves", "11988887777");

		// Marca presenca em todas as aulas menos a 3 (1 falta so no modulo
		// Plataforma, os outros dois completos) - apto, mas com reposicao
		// recomendada.
		for (int numero = 1; numero <= 9; numero++) {
			if (numero != 3) {
				marcarPresencaManual(CPF_COM_FALTA, numero);
			}
		}

		mockMvc.perform(get("/api/turmas/{turmaId}/painel", turma.getId())
				.with(autenticado()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.linhas", org.hamcrest.Matchers.hasSize(1)))
				.andExpect(jsonPath("$.linhas[0].nome").value("Pedro Alves"))
				.andExpect(jsonPath("$.linhas[0].formatura.statusGeral").value("APTO_REPOR_RECOMENDADO"))
				.andExpect(jsonPath("$.linhas[0].formatura.aRepor", org.hamcrest.Matchers.hasSize(1)))
				.andExpect(jsonPath("$.linhas[0].formatura.aRepor[0].numero").value(3));
	}

	@Test
	void deveGerarLinkWhatsappQuandoHaReposicaoPendente() throws Exception {
		cadastrarParticipante(CPF_COM_FALTA, "Pedro Alves", "11988887777");
		marcarPresencaManual(CPF_COM_FALTA, 1);

		var participanteId = participanteRepository.findByCpf(CPF_COM_FALTA).orElseThrow().getId();

		mockMvc.perform(get("/api/turmas/{turmaId}/painel/{participanteId}/link-whatsapp", turma.getId(), participanteId)
				.with(autenticado()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.link", org.hamcrest.Matchers.containsString("https://wa.me/5511988887777")));
	}

	@Test
	void deveRetornar400QuandoNaoHaReposicaoPendenteParaNotificar() throws Exception {
		cadastrarParticipante(CPF_COMPLETO, "Joao Silva", "11999999999");
		for (int numero = 1; numero <= 9; numero++) {
			marcarPresencaManual(CPF_COMPLETO, numero);
		}
		var participanteId = participanteRepository.findByCpf(CPF_COMPLETO).orElseThrow().getId();

		mockMvc.perform(get("/api/turmas/{turmaId}/painel/{participanteId}/link-whatsapp", turma.getId(), participanteId)
				.with(autenticado()))
				.andExpect(status().isBadRequest());
	}

	@Test
	void deveAvancarParaProximaAulaAoEncerrarManualmente() throws Exception {
		definirAgenda(obterTipoAulaPorNumero(1), LocalDate.now());
		definirAgenda(obterTipoAulaPorNumero(2), LocalDate.now().plusDays(7));

		mockMvc.perform(patch("/api/turmas/{turmaId}/agenda/atual/encerrar", turma.getId())
				.with(autenticado()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.numero").value(2))
				.andExpect(jsonPath("$.status").value("ATIVA"));
	}

	@Test
	void deveRetornar204QuandoEncerraAUltimaAulaDaAgenda() throws Exception {
		definirAgenda(obterTipoAulaPorNumero(1), LocalDate.now());

		mockMvc.perform(patch("/api/turmas/{turmaId}/agenda/atual/encerrar", turma.getId())
				.with(autenticado()))
				.andExpect(status().isNoContent());
	}

	private void marcarPresencaManual(String cpf, int numeroTipoAula) throws Exception {
		definirAgenda(obterTipoAulaPorNumero(numeroTipoAula), LocalDate.now().minusWeeks(9 - numeroTipoAula));
		String aulaId = aulaRepository.findByTurmaIdAndTipoAulaId(turma.getId(), obterTipoAulaPorNumero(numeroTipoAula))
				.orElseThrow().getId().toString();
		mockMvc.perform(post("/api/aulas/{aulaId}/presencas/manual", aulaId)
				.with(autenticado())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"cpf": "%s"}
						""".formatted(cpf)))
				.andExpect(status().isCreated());
	}

	private void definirAgenda(UUID tipoAulaId, LocalDate data) throws Exception {
		mockMvc.perform(put("/api/turmas/{turmaId}/agenda", turma.getId())
				.with(autenticado())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"itens": [{"tipoAulaId": "%s", "data": "%s"}]}
						""".formatted(tipoAulaId, data)));
	}

	private void cadastrarParticipante(String cpf, String nome, String celular) throws Exception {
		mockMvc.perform(post("/api/publico/participantes")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{
						  "nome": "%s",
						  "cpf": "%s",
						  "celular": "%s",
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
						""".formatted(nome, cpf, celular)))
				.andExpect(status().isCreated());
	}

	private Turma criarTurma(StatusTurma status) {
		Curso curso = cursoRepository.findById(obterCursoEscolaHombridade()).orElseThrow();

		Turma novaTurma = new Turma();
		novaTurma.setCurso(curso);
		novaTurma.setNome("Campus 85 - Turma " + System.nanoTime());
		novaTurma.setTokenCadastro("TK" + System.nanoTime() % 1_000_000);
		novaTurma.setStatus(status);
		return turmaRepository.save(novaTurma);
	}

}
