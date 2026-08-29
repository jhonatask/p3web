package br.com.familychurch.p3.participante.service;

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
import br.com.familychurch.p3.participante.dto.ParticipanteResumoResponse;
import br.com.familychurch.p3.participante.exception.ConsentimentoLgpdObrigatorioException;
import br.com.familychurch.p3.participante.exception.ParticipanteNaoEncontradoException;
import br.com.familychurch.p3.participante.exception.QuaisGcsObrigatorioException;
import br.com.familychurch.p3.participante.exception.QuantidadeFilhosObrigatoriaException;
import br.com.familychurch.p3.participante.repository.ParticipanteRepository;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ParticipanteServiceImpl implements ParticipanteService {

	private final ParticipanteRepository participanteRepository;
	private final HistoricoItemRepository historicoItemRepository;
	private final GeocodingService geocodingService;

	public ParticipanteServiceImpl(ParticipanteRepository participanteRepository,
			HistoricoItemRepository historicoItemRepository, GeocodingService geocodingService) {
		this.participanteRepository = participanteRepository;
		this.historicoItemRepository = historicoItemRepository;
		this.geocodingService = geocodingService;
	}

	@Override
	public ParticipanteCadastroResponse cadastrar(ParticipanteCadastroRequest request) {
		validarConsentimentoLgpd(request.aceiteLgpd());
		validarQuantidadeFilhos(request.temFilhos(), request.quantidadeFilhos());
		validarQuaisGcs(request.participaGc(), request.quaisGcs());

		String cpf = request.cpf().replaceAll("\\D", "");
		Participante participante = participanteRepository.findByCpf(cpf).orElseGet(Participante::new);

		preencherDadosDoFormulario(participante, request, cpf);
		geocodificar(participante.getEndereco());

		participante = participanteRepository.save(participante);
		registrarHistorico(participante, request);

		return ParticipanteCadastroResponse.from(participante);
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
	@Transactional(readOnly = true)
	public ParticipanteResumoResponse buscarResumoPorCpf(String cpf) {
		String cpfLimpo = cpf.replaceAll("\\D", "");
		Participante participante = participanteRepository.findByCpf(cpfLimpo)
				.orElseThrow(() -> new ParticipanteNaoEncontradoException(cpfLimpo));
		return ParticipanteResumoResponse.from(participante);
	}

	@Override
	@Transactional(readOnly = true)
	public ParticipanteEdicaoResponse buscarParaEdicaoPorCpf(String cpf) {
		String cpfLimpo = cpf.replaceAll("\\D", "");
		Participante participante = participanteRepository.findByCpf(cpfLimpo)
				.orElseThrow(() -> new ParticipanteNaoEncontradoException(cpfLimpo));
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
