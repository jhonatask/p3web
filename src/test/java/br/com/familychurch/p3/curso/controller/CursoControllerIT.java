package br.com.familychurch.p3.curso.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.familychurch.p3.IntegrationTestSupport;
import br.com.familychurch.p3.aluno.repository.AlunoRepository;
import br.com.familychurch.p3.aula.repository.AulaRepository;
import br.com.familychurch.p3.curso.repository.CursoRepository;
import br.com.familychurch.p3.presenca.repository.PresencaRepository;
import br.com.familychurch.p3.turma.repository.TurmaRepository;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

class CursoControllerIT extends IntegrationTestSupport {

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

	@BeforeEach
	void configurarCenario() throws Exception {
		presencaRepository.deleteAll();
		aulaRepository.deleteAll();
		alunoRepository.deleteAll();
		turmaRepository.deleteAll();
		cursoRepository.deleteAll();
		tokenAdmin = obterTokenAdmin();
	}

	@Test
	void deveCriarCursoComSucesso() throws Exception {
		mockMvc.perform(post("/api/cursos")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAdmin)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"nome": "Escola de Hombridade", "descricao": "Curso base"}
						"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.nome").value("Escola de Hombridade"))
				.andExpect(jsonPath("$.ativo").value(true));
	}

	@Test
	void deveRetornar409QuandoCursoJaExisteComMesmoNome() throws Exception {
		criarCurso("Escola de Hombridade");

		mockMvc.perform(post("/api/cursos")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAdmin)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"nome": "Escola de Hombridade", "descricao": "Duplicado"}
						"""))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.status").value(409));
	}

	@Test
	void deveRetornar404QuandoCursoNaoExiste() throws Exception {
		UUID idInexistente = UUID.randomUUID();

		mockMvc.perform(get("/api/cursos/{id}", idInexistente)
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAdmin))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status").value(404));
	}

	@Test
	void deveListarApenasCursosAtivosQuandoFiltroAtivoTrue() throws Exception {
		String idCursoInativo = criarCurso("Curso Inativo");
		criarCurso("Curso Ativo");
		mockMvc.perform(patch("/api/cursos/{id}/desativar", idCursoInativo)
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAdmin));

		mockMvc.perform(get("/api/cursos").param("ativo", "true")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAdmin))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].nome").value("Curso Ativo"));
	}

	@Test
	void deveRetornar400ComErroDeValidacaoQuandoNomeEmBranco() throws Exception {
		mockMvc.perform(post("/api/cursos")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAdmin)
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
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAdmin)
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"nome\": \"%s\"}".formatted(nome)))
				.andReturn();
		return objectMapper.readTree(resultado.getResponse().getContentAsString()).get("id").asText();
	}

}
