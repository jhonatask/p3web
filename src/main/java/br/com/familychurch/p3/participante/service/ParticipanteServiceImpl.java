package br.com.familychurch.p3.participante.service;

import br.com.familychurch.p3.common.email.EmailService;
import br.com.familychurch.p3.common.token.GeradorToken;
import br.com.familychurch.p3.historico.HistoricoItem;
import br.com.familychurch.p3.historico.OrigemHistorico;
import br.com.familychurch.p3.historico.StatusHistorico;
import br.com.familychurch.p3.historico.TipoHistorico;
import br.com.familychurch.p3.historico.dto.HistoricoItemResponse;
import br.com.familychurch.p3.historico.repository.HistoricoItemRepository;
import br.com.familychurch.p3.participante.Endereco;
import br.com.familychurch.p3.participante.Participante;
import br.com.familychurch.p3.participante.dto.ParticipanteCadastroRequest;
import br.com.familychurch.p3.participante.dto.ParticipanteCadastroResponse;
import br.com.familychurch.p3.participante.dto.ParticipanteEdicaoResponse;
import br.com.familychurch.p3.participante.dto.ParticipanteResponse;
import br.com.familychurch.p3.participante.exception.ConsentimentoLgpdObrigatorioException;
import br.com.familychurch.p3.participante.exception.ParticipanteNaoEncontradoException;
import br.com.familychurch.p3.participante.exception.QuaisGcsObrigatorioException;
import br.com.familychurch.p3.participante.exception.QuantidadeFilhosObrigatoriaException;
import br.com.familychurch.p3.participante.exception.TokenAtualizacaoInvalidoException;
import br.com.familychurch.p3.participante.repository.ParticipanteRepository;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ParticipanteServiceImpl implements ParticipanteService {

	private static final Logger LOGGER = LoggerFactory.getLogger(ParticipanteServiceImpl.class);
	private static final int TAMANHO_TOKEN = 8;
	private static final Duration VALIDADE_TOKEN_MATRICULA = Duration.ofHours(2);
	private static final Duration VALIDADE_TOKEN_ATUALIZACAO = Duration.ofHours(24);

	private final ParticipanteRepository participanteRepository;
	private final HistoricoItemRepository historicoItemRepository;
	private final GeocodingService geocodingService;
	private final EmailService emailService;
	private final String urlAtualizacaoCadastro;

	public ParticipanteServiceImpl(ParticipanteRepository participanteRepository,
			HistoricoItemRepository historicoItemRepository, GeocodingService geocodingService,
			EmailService emailService,
			@Value("${app.atualizacao-cadastro.base-url}") String urlAtualizacaoCadastro) {
		this.participanteRepository = participanteRepository;
		this.historicoItemRepository = historicoItemRepository;
		this.geocodingService = geocodingService;
		this.emailService = emailService;
		this.urlAtualizacaoCadastro = urlAtualizacaoCadastro;
	}

	@Override
	public ParticipanteCadastroResponse cadastrar(ParticipanteCadastroRequest request) {
		validarConsentimentoLgpd(request.aceiteLgpd());
		validarQuantidadeFilhos(request.temFilhos(), request.quantidadeFilhos());
		validarQuaisGcs(request.participaGc(), request.quaisGcs());

		String cpf = request.cpf().replaceAll("\\D", "");
		Participante existente = participanteRepository.findByCpf(cpf).orElse(null);
		if (existente != null) {
			validarTokenAtualizacao(existente, request.token());
		}
		Participante participante = existente != null ? existente : new Participante();

		preencherDadosDoFormulario(participante, request, cpf);
		geocodificar(participante.getEndereco());
		participante.setTokenMatricula(GeradorToken.gerar(TAMANHO_TOKEN));
		participante.setTokenMatriculaExpiraEm(LocalDateTime.now().plus(VALIDADE_TOKEN_MATRICULA));

		participante = participanteRepository.save(participante);
		registrarHistorico(participante, request);

		return ParticipanteCadastroResponse.from(participante);
	}

	private void validarTokenAtualizacao(Participante participante, String token) {
		String tokenEsperado = participante.getTokenAtualizacao();
		LocalDateTime expiraEm = participante.getTokenAtualizacaoExpiraEm();
		boolean valido = tokenEsperado != null && tokenEsperado.equals(token)
				&& expiraEm != null && expiraEm.isAfter(LocalDateTime.now());
		if (!valido) {
			throw new TokenAtualizacaoInvalidoException();
		}
	}

	@Override
	@Transactional(readOnly = true)
	public ParticipanteResponse buscarPorCpf(String cpf) {
		String cpfLimpo = cpf.replaceAll("\\D", "");
		Participante participante = participanteRepository.findByCpf(cpfLimpo)
				.orElseThrow(() -> new ParticipanteNaoEncontradoException(cpfLimpo));
		List<HistoricoItemResponse> historico = historicoItemRepository.findByParticipanteId(participante.getId())
				.stream()
				.map(HistoricoItemResponse::from)
				.toList();
		return ParticipanteResponse.from(participante, historico);
	}

	@Override
	public void solicitarAtualizacaoCadastro(String cpf) {
		String cpfLimpo = cpf.replaceAll("\\D", "");
		Participante participante = participanteRepository.findByCpf(cpfLimpo).orElse(null);
		if (participante == null || participante.getEmail() == null || participante.getEmail().isBlank()) {
			return;
		}

		String token = GeradorToken.gerar(TAMANHO_TOKEN);
		participante.setTokenAtualizacao(token);
		participante.setTokenAtualizacaoExpiraEm(LocalDateTime.now().plus(VALIDADE_TOKEN_ATUALIZACAO));
		participanteRepository.save(participante);

		String link = "%s?token=%s".formatted(urlAtualizacaoCadastro, token);
		String corpoHtml = """
				<p>Olá, %s!</p>
				<p>Clique no link abaixo para atualizar seu cadastro:</p>
				<p><a href="%s">%s</a></p>
				<p>Se você não pediu essa atualização, pode ignorar este e-mail.</p>
				""".formatted(participante.getNome(), link, link);
		try {
			emailService.enviar(participante.getEmail(), "Atualize seu cadastro", corpoHtml);
		} catch (RuntimeException exception) {
			LOGGER.error("Falha ao enviar e-mail de atualizacao de cadastro para participante {}",
					participante.getId(), exception);
		}
	}

	@Override
	@Transactional(readOnly = true)
	public ParticipanteEdicaoResponse buscarParaEdicaoPorToken(String token) {
		Participante participante = participanteRepository.findByTokenAtualizacao(token)
				.filter(candidato -> candidato.getTokenAtualizacaoExpiraEm() != null
						&& candidato.getTokenAtualizacaoExpiraEm().isAfter(LocalDateTime.now()))
				.orElseThrow(TokenAtualizacaoInvalidoException::new);
		List<HistoricoItem> historico = historicoItemRepository.findByParticipanteId(participante.getId());
		Set<String> cursosRealizados = itensDoHistorico(historico, TipoHistorico.CURSO);
		Set<String> eventosParticipou = itensDoHistorico(historico, TipoHistorico.EVENTO);
		return ParticipanteEdicaoResponse.from(participante, cursosRealizados, eventosParticipou);
	}

	private Set<String> itensDoHistorico(List<HistoricoItem> historico, TipoHistorico tipo) {
		return historico.stream()
				.filter(item -> item.getTipo() == tipo && item.getOrigem() == OrigemHistorico.CADASTRO)
				.map(HistoricoItem::getItem)
				.collect(Collectors.toCollection(HashSet::new));
	}

	private void preencherDadosDoFormulario(Participante participante, ParticipanteCadastroRequest request, String cpf) {
		participante.setNome(request.nome());
		participante.setCpf(cpf);
		participante.setCelular(request.celular().replaceAll("\\D", ""));
		participante.setEmail(request.email().trim());
		participante.setDataNascimento(request.dataNascimento());
		participante.setEstadoCivil(request.estadoCivil());
		participante.setTemFilhos(request.temFilhos());
		participante.setQuantidadeFilhos(request.temFilhos() ? request.quantidadeFilhos() : null);

		// Hibernate materializa o @Embedded como null quando todas as colunas
		// estao null (caso de um Participante minimo criado pelo fluxo de
		// Aluno, que nao coleta endereco) - por isso sempre criamos uma nova
		// instancia aqui em vez de reaproveitar participante.getEndereco().
		Endereco endereco = new Endereco();
		endereco.setCep(request.cep());
		endereco.setRua(request.rua());
		endereco.setNumero(request.numero());
		endereco.setComplemento(request.complemento());
		endereco.setBairro(request.bairro());
		endereco.setCidade(request.cidade());
		endereco.setUf(request.uf());
		endereco.setPontoReferencia(request.pontoReferencia());
		participante.setEndereco(endereco);

		participante.setTempoNaFamily(request.tempoNaFamily());
		participante.setBatizado(request.batizado());
		participante.setServeMinisterio(request.serveMinisterio());
		participante.setQualMinisterioServe(request.serveMinisterio() ? request.qualMinisterioServe() : null);
		participante.setInstagram(request.instagram());
		participante.setFacebook(request.facebook());
		participante.setParticipaGc(request.participaGc());
		participante.setQuaisGcs(
				request.participaGc() && request.quaisGcs() != null ? new HashSet<>(request.quaisGcs()) : new HashSet<>());

		participante.setConsentimentoLgpd(true);
		participante.setConsentimentoLgpdTimestamp(LocalDateTime.now());
	}

	private void geocodificar(Endereco endereco) {
		EnderecoParaGeocodificar enderecoParaGeocodificar = new EnderecoParaGeocodificar(
				endereco.getCep(), endereco.getRua(), endereco.getNumero(),
				endereco.getBairro(), endereco.getCidade(), endereco.getUf());
		geocodingService.geocodificar(enderecoParaGeocodificar).ifPresent(coordenadas -> {
			endereco.setLatitude(coordenadas.latitude());
			endereco.setLongitude(coordenadas.longitude());
		});
	}

	private void registrarHistorico(Participante participante, ParticipanteCadastroRequest request) {
		registrarItens(participante, request.cursosRealizados(), TipoHistorico.CURSO);
		registrarItem(participante, request.cursosOutros(), TipoHistorico.CURSO);
		registrarItens(participante, request.eventosParticipou(), TipoHistorico.EVENTO);
		registrarItem(participante, request.eventosOutros(), TipoHistorico.EVENTO);
	}

	private void registrarItens(Participante participante, Set<String> itens, TipoHistorico tipo) {
		if (itens == null) {
			return;
		}
		itens.forEach(item -> registrarItem(participante, item, tipo));
	}

	private void registrarItem(Participante participante, String item, TipoHistorico tipo) {
		if (item == null || item.isBlank()) {
			return;
		}
		boolean jaExiste = historicoItemRepository.existsByParticipanteIdAndItemAndTipoAndOrigem(
				participante.getId(), item, tipo, OrigemHistorico.CADASTRO);
		if (jaExiste) {
			return;
		}
		HistoricoItem historicoItem = new HistoricoItem();
		historicoItem.setParticipante(participante);
		historicoItem.setItem(item);
		historicoItem.setTipo(tipo);
		historicoItem.setStatus(StatusHistorico.CONCLUIDO);
		historicoItem.setOrigem(OrigemHistorico.CADASTRO);
		historicoItemRepository.save(historicoItem);
	}

	private void validarConsentimentoLgpd(boolean aceiteLgpd) {
		if (!aceiteLgpd) {
			throw new ConsentimentoLgpdObrigatorioException();
		}
	}

	private void validarQuantidadeFilhos(boolean temFilhos, Integer quantidadeFilhos) {
		if (temFilhos && quantidadeFilhos == null) {
			throw new QuantidadeFilhosObrigatoriaException();
		}
	}

	private void validarQuaisGcs(boolean participaGc, Set<String> quaisGcs) {
		if (participaGc && (quaisGcs == null || quaisGcs.isEmpty())) {
			throw new QuaisGcsObrigatorioException();
		}
	}

}
