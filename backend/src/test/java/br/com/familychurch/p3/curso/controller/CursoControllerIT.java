package br.com.familychurch.p3.curso.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.familychurch.p3.IntegrationTestSupport;
import br.com.familychurch.p3.aluno.repository.AlunoRepository;
import br.com.familychurch.p3.aula.repository.AulaRepository;
import br.com.familychurch.p3.avaliacao.repository.AvaliacaoRepository;
import br.com.familychurch.p3.presenca.repository.PresencaRepository;
import br.com.familychurch.p3.turma.repository.TurmaRepository;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

class CursoControllerIT extends IntegrationTestSupport {

	@Autowired
	private MockMvc mockMvc;

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

	@BeforeEach
	void configurarCenario() {
		avaliacaoRepository.deleteAll();
		presencaRepository.deleteAll();
		aulaRepository.deleteAll();
		alunoRepository.deleteAll();
		turmaRepository.deleteAll();
		// Nao apaga curso: "Curso de Teste" e dado de referencia
		// semeado pela migration V15 (tem tipo_aula dependente) - os testes
		// aqui usam nomes de curso proprios, sem colidir com ele.
	}

	@Test
	void deveCriarCursoComSucesso() throws Exception {
		String nome = "Curso de Teste " + System.nanoTime();
		mockMvc.perform(post("/api/cursos")
				.with(autenticado())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"nome": "%s", "descricao": "Curso base"}
						""".formatted(nome)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.nome").value(nome))
				.andExpect(jsonPath("$.ativo").value(true));
	}

	@Test
	void deveRetornar409QuandoCursoJaExisteComMesmoNome() throws Exception {
		String nome = "Curso de Teste " + System.nanoTime();
		criarCurso(nome);

		mockMvc.perform(post("/api/cursos")
				.with(autenticado())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"nome": "%s", "descricao": "Duplicado"}
						""".formatted(nome)))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.status").value(409));
	}

	@Test
	void deveRetornar404QuandoCursoNaoExiste() throws Exception {
		UUID idInexistente = UUID.randomUUID();

		mockMvc.perform(get("/api/cursos/{id}", idInexistente)
				.with(autenticado()))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status").value(404));
	}

	@Test
	void deveListarApenasCursosAtivosQuandoFiltroAtivoTrue() throws Exception {
		long sufixo = System.nanoTime();
		String nomeInativo = "Curso Inativo " + sufixo;
		String nomeAtivo = "Curso Ativo " + sufixo;
		String idCursoInativo = criarCurso(nomeInativo);
		criarCurso(nomeAtivo);
		mockMvc.perform(patch("/api/cursos/{id}/desativar", idCursoInativo)
				.with(autenticado()));

		// Nao assume lista exclusiva: curso e dado misto (referencia semeada
		// + dado de teste, nunca limpo por completo entre testes desta
		// classe) - so verifica presenca/ausencia dos nomes desta execucao.
		var resultado = mockMvc.perform(get("/api/cursos").param("ativo", "true")
				.with(autenticado()))
				.andExpect(status().isOk())
				.andReturn();
		var nomes = objectMapper.readTree(resultado.getResponse().getContentAsString());
		var nomesAtivos = new java.util.ArrayList<String>();
		nomes.forEach(node -> nomesAtivos.add(node.get("nome").asText()));

		assertThat(nomesAtivos).contains(nomeAtivo).doesNotContain(nomeInativo);
	}

	@Test
	void deveRetornar400ComErroDeValidacaoQuandoNomeEmBranco() throws Exception {
		mockMvc.perform(post("/api/cursos")
				.with(autenticado())
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"nome": "", "descricao": "sem nome"}
						"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.erros[0].campo").value("nome"));
	}

	@Test
	void deveRetornarNaoAutorizadoQuandoRequisicaoSemToken() throws Exception {
		mockMvc.perform(get("/api/cursos"))
				.andExpect(status().is4xxClientError());
	}

	private String criarCurso(String nome) throws Exception {
		var resultado = mockMvc.perform(post("/api/cursos")
				.with(autenticado())
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"nome\": \"%s\"}".formatted(nome)))
				.andReturn();
		return objectMapper.readTree(resultado.getResponse().getContentAsString()).get("id").asText();
	}

}
