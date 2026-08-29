package br.com.familychurch.p3.participante.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.familychurch.p3.IntegrationTestSupport;
import br.com.familychurch.p3.aluno.repository.AlunoRepository;
import br.com.familychurch.p3.avaliacao.repository.AvaliacaoRepository;
import br.com.familychurch.p3.common.email.EmailService;
import br.com.familychurch.p3.historico.repository.HistoricoItemRepository;
import br.com.familychurch.p3.participante.repository.ParticipanteRepository;
import br.com.familychurch.p3.presenca.repository.PresencaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

class ParticipanteControllerIT extends IntegrationTestSupport {

	private static final String CPF_VALIDO = "52998224725";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ParticipanteRepository participanteRepository;

	@Autowired
	private HistoricoItemRepository historicoItemRepository;

	@Autowired
	private AlunoRepository alunoRepository;

	@Autowired
	private AvaliacaoRepository avaliacaoRepository;

	@Autowired
	private PresencaRepository presencaRepository;

	@MockitoBean
	private EmailService emailService;

	@BeforeEach
	void configurarCenario() {
		avaliacaoRepository.deleteAll();
		presencaRepository.deleteAll();
		historicoItemRepository.deleteAll();
		alunoRepository.deleteAll();
		participanteRepository.deleteAll();
	}

	@Test
	void deveCadastrarParticipanteComDadosCompletos() throws Exception {
		mockMvc.perform(post("/api/publico/participantes")
				.contentType(MediaType.APPLICATION_JSON)
				.content(payloadCompleto(CPF_VALIDO, "Joao Silva")))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.nome").value("Joao Silva"))
				.andExpect(jsonPath("$.cpf").value(CPF_VALIDO));
	}

	@Test
	void deveRetornar400QuandoAceiteLgpdFalse() throws Exception {
		mockMvc.perform(post("/api/publico/participantes")
				.contentType(MediaType.APPLICATION_JSON)
				.content(payload(CPF_VALIDO, "Joao Silva", true, 2, true, true, false, null)))
				.andExpect(status().isBadRequest());
	}

	@Test
	void deveRetornar400QuandoCpfInvalido() throws Exception {
		mockMvc.perform(post("/api/publico/participantes")
				.contentType(MediaType.APPLICATION_JSON)
				.content(payloadCompleto("52998224700", "Joao Silva")))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.erros[0].campo").value("cpf"));
	}

	@Test
	void deveRetornar400QuandoTemFilhosSemQuantidade() throws Exception {
		mockMvc.perform(post("/api/publico/participantes")
				.contentType(MediaType.APPLICATION_JSON)
				.content(payload(CPF_VALIDO, "Joao Silva", true, null, true, true, true, null)))
				.andExpect(status().isBadRequest());
	}

	@Test
	void deveRetornar400QuandoParticipaGcSemQuaisGcs() throws Exception {
		mockMvc.perform(post("/api/publico/participantes")
				.contentType(MediaType.APPLICATION_JSON)
				.content(payloadComParticipaGcSemQuaisGcs(CPF_VALIDO)))
				.andExpect(status().isBadRequest());
	}

	@Test
	void deveRetornar403AoReenviarCpfExistenteSemToken() throws Exception {
		mockMvc.perform(post("/api/publico/participantes")
				.contentType(MediaType.APPLICATION_JSON)
				.content(payloadCompleto(CPF_VALIDO, "Joao Silva")))
				.andExpect(status().isCreated());

		mockMvc.perform(post("/api/publico/participantes")
				.contentType(MediaType.APPLICATION_JSON)
				.content(payloadCompleto(CPF_VALIDO, "Joao Silva Atualizado")))
				.andExpect(status().isForbidden());

		assertThat(participanteRepository.findByCpf(CPF_VALIDO).orElseThrow().getNome()).isEqualTo("Joao Silva");
	}

	@Test
	void deveAtualizarParticipanteExistenteComTokenValido() throws Exception {
		mockMvc.perform(post("/api/publico/participantes")
				.contentType(MediaType.APPLICATION_JSON)
				.content(payloadCompleto(CPF_VALIDO, "Joao Silva")))
				.andExpect(status().isCreated());

		Mockito.reset(emailService);
		mockMvc.perform(post("/api/publico/participantes/solicitar-atualizacao")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"cpf": "%s"}
						""".formatted(CPF_VALIDO)))
				.andExpect(status().isOk());
		String token = participanteRepository.findByCpf(CPF_VALIDO).orElseThrow().getTokenAtualizacao();

		mockMvc.perform(post("/api/publico/participantes")
				.contentType(MediaType.APPLICATION_JSON)
				.content(payloadCompleto(CPF_VALIDO, "Joao Silva Atualizado", token)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.nome").value("Joao Silva Atualizado"));

		assertThat(participanteRepository.count()).isEqualTo(1);
	}

	@Test
	void deveDevolverTokenMatriculaAoCadastrar() throws Exception {
		mockMvc.perform(post("/api/publico/participantes")
				.contentType(MediaType.APPLICATION_JSON)
				.content(payloadCompleto(CPF_VALIDO, "Joao Silva")))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.tokenMatricula").isNotEmpty());
	}

	@Test
	void deveEnviarEmailAoSolicitarAtualizacaoDeParticipanteExistente() throws Exception {
		mockMvc.perform(post("/api/publico/participantes")
				.contentType(MediaType.APPLICATION_JSON)
				.content(payloadCompleto(CPF_VALIDO, "Joao Silva")))
				.andExpect(status().isCreated());
		Mockito.reset(emailService);

		mockMvc.perform(post("/api/publico/participantes/solicitar-atualizacao")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"cpf": "%s"}
						""".formatted(CPF_VALIDO)))
				.andExpect(status().isOk());

		Mockito.verify(emailService).enviar(Mockito.eq("joao@example.com"), Mockito.anyString(), Mockito.anyString());
		assertThat(participanteRepository.findByCpf(CPF_VALIDO).orElseThrow().getTokenAtualizacao()).isNotNull();
	}

	@Test
	void naoDeveEnviarEmailNemVazarQuandoCpfNaoCadastradoAoSolicitarAtualizacao() throws Exception {
		mockMvc.perform(post("/api/publico/participantes/solicitar-atualizacao")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"cpf": "%s"}
						""".formatted(CPF_VALIDO)))
				.andExpect(status().isOk());

		Mockito.verifyNoInteractions(emailService);
	}

	@Test
	void deveBuscarCadastroPorTokenValido() throws Exception {
		mockMvc.perform(post("/api/publico/participantes")
				.contentType(MediaType.APPLICATION_JSON)
				.content(payloadCompleto(CPF_VALIDO, "Joao Silva")))
				.andExpect(status().isCreated());
		mockMvc.perform(post("/api/publico/participantes/solicitar-atualizacao")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"cpf": "%s"}
						""".formatted(CPF_VALIDO)));
		String token = participanteRepository.findByCpf(CPF_VALIDO).orElseThrow().getTokenAtualizacao();

		mockMvc.perform(get("/api/publico/participantes/cadastro").param("token", token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.nome").value("Joao Silva"));
	}

	@Test
	void deveRetornar403AoBuscarCadastroComTokenInvalido() throws Exception {
		mockMvc.perform(get("/api/publico/participantes/cadastro").param("token", "TOKEN-INEXISTENTE"))
				.andExpect(status().isForbidden());
	}

	@Test
	void deveRetornarNaoAutorizadoAoConsultarSemToken() throws Exception {
		mockMvc.perform(get("/api/participantes/{cpf}", CPF_VALIDO))
				.andExpect(status().is4xxClientError());
	}

	@Test
	void deveConsultarParticipantePorCpfComHistoricoDosCursosEEventos() throws Exception {
		mockMvc.perform(post("/api/publico/participantes")
				.contentType(MediaType.APPLICATION_JSON)
				.content(payloadCompleto(CPF_VALIDO, "Joao Silva")))
				.andExpect(status().isCreated());

		mockMvc.perform(get("/api/participantes/{cpf}", CPF_VALIDO)
				.with(autenticado()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.nome").value("Joao Silva"))
				.andExpect(jsonPath("$.endereco.bairro").value("Centro"))
				.andExpect(jsonPath("$.historico", hasSize(2)));
	}

	@Test
	void deveRetornar404AoConsultarCpfNaoCadastrado() throws Exception {
		mockMvc.perform(get("/api/participantes/{cpf}", CPF_VALIDO)
				.with(autenticado()))
				.andExpect(status().isNotFound());
	}

	@Test
	void deveAceitarCadastroSemNenhumCursoOuEvento() throws Exception {
		mockMvc.perform(post("/api/publico/participantes")
				.contentType(MediaType.APPLICATION_JSON)
				.content(payloadSemCursosNemEventos(CPF_VALIDO)))
				.andExpect(status().isCreated());
	}

	@Test
	void deveAceitarCadastroQuandoUsaApenasCamposOutros() throws Exception {
		mockMvc.perform(post("/api/publico/participantes")
				.contentType(MediaType.APPLICATION_JSON)
				.content(payloadComApenasOutros(CPF_VALIDO)))
				.andExpect(status().isCreated());
	}

	@Test
	void devePersistirConsentimentoLgpdComTimestamp() throws Exception {
		mockMvc.perform(post("/api/publico/participantes")
				.contentType(MediaType.APPLICATION_JSON)
				.content(payloadCompleto(CPF_VALIDO, "Joao Silva")))
				.andExpect(status().isCreated());

		mockMvc.perform(get("/api/participantes/{cpf}", CPF_VALIDO)
				.with(autenticado()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.consentimentoLgpd").value(true))
				.andExpect(jsonPath("$.consentimentoLgpdTimestamp").exists());
	}

	private String payloadCompleto(String cpf, String nome) {
		return payload(cpf, nome, true, 2, true, true, true, null);
	}

	private String payloadCompleto(String cpf, String nome, String token) {
		return payload(cpf, nome, true, 2, true, true, true, token);
	}

	private String payloadComParticipaGcSemQuaisGcs(String cpf) {
		return """
				{
				  "nome": "Joao Silva",
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
				  "participaGc": true,
				  "aceiteLgpd": true
				}
				"""
				.formatted(cpf);
	}

	private String payloadSemCursosNemEventos(String cpf) {
		return """
				{
				  "nome": "Joao Silva",
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
				  "participaGc": false,
				  "aceiteLgpd": true
				}
				"""
				.formatted(cpf);
	}

	private String payloadComApenasOutros(String cpf) {
		return """
				{
				  "nome": "Joao Silva",
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
				"""
				.formatted(cpf);
	}

	private String payload(String cpf, String nome, boolean temFilhos, Integer quantidadeFilhos,
			boolean batizado, boolean participaGc, boolean aceiteLgpd, String token) {
		String tokenJson = token == null ? "null" : "\"%s\"".formatted(token);
		return """
				{
				  "nome": "%s",
				  "cpf": "%s",
				  "celular": "62999998888",
				  "email": "joao@example.com",
				  "dataNascimento": "1990-05-20",
				  "estadoCivil": "SOLTEIRO",
				  "temFilhos": %s,
				  "quantidadeFilhos": %s,
				  "cep": "74000-000",
				  "rua": "Rua das Flores",
				  "numero": "123",
				  "complemento": "",
				  "bairro": "Centro",
				  "cidade": "Goiania",
				  "uf": "GO",
				  "pontoReferencia": "",
				  "tempoNaFamily": "MAIS_DE_3_ANOS",
				  "batizado": %s,
				  "serveMinisterio": false,
				  "qualMinisterioServe": "",
				  "instagram": "@joaosilva",
				  "facebook": "",
				  "cursosRealizados": ["Hombridade"],
				  "cursosOutros": "",
				  "eventosParticipou": ["Awake"],
				  "eventosOutros": "",
				  "participaGc": %s,
				  "quaisGcs": ["GC de Homens"],
				  "aceiteLgpd": %s,
				  "token": %s
				}
				"""
				.formatted(nome, cpf, temFilhos, quantidadeFilhos, batizado, participaGc, aceiteLgpd, tokenJson);
	}

}
