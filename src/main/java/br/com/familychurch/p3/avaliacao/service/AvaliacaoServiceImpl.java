package br.com.familychurch.p3.avaliacao.service;

import br.com.familychurch.p3.aula.Aula;
import br.com.familychurch.p3.aula.repository.AulaRepository;
import br.com.familychurch.p3.avaliacao.Avaliacao;
import br.com.familychurch.p3.avaliacao.dto.AvaliacaoRequest;
import br.com.familychurch.p3.avaliacao.dto.AvaliacaoResponse;
import br.com.familychurch.p3.avaliacao.exception.AulaNaoEncontradaException;
import br.com.familychurch.p3.avaliacao.exception.AvaliacaoDuplicadaException;
import br.com.familychurch.p3.avaliacao.exception.CheckinNaoRealizadoException;
import br.com.familychurch.p3.avaliacao.repository.AvaliacaoRepository;
import br.com.familychurch.p3.participante.Participante;
import br.com.familychurch.p3.participante.exception.ParticipanteNaoEncontradoException;
import br.com.familychurch.p3.participante.repository.ParticipanteRepository;
import br.com.familychurch.p3.presenca.repository.PresencaRepository;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AvaliacaoServiceImpl implements AvaliacaoService {

	private static final Set<Integer> NUMEROS_AULA_DESPERTAR_PARA_SERVIR = Set.of(3, 6, 9);

	private final AvaliacaoRepository avaliacaoRepository;
	private final AulaRepository aulaRepository;
	private final ParticipanteRepository participanteRepository;
	private final PresencaRepository presencaRepository;

	public AvaliacaoServiceImpl(AvaliacaoRepository avaliacaoRepository, AulaRepository aulaRepository,
			ParticipanteRepository participanteRepository, PresencaRepository presencaRepository) {
		this.avaliacaoRepository = avaliacaoRepository;
		this.aulaRepository = aulaRepository;
		this.participanteRepository = participanteRepository;
		this.presencaRepository = presencaRepository;
	}

	@Override
	public AvaliacaoResponse registrar(UUID aulaId, AvaliacaoRequest request) {
		Aula aula = buscarAulaPorId(aulaId);
		Participante participante = buscarParticipantePorCpf(request.cpf());
		validarPresencaRegistrada(participante.getId(), aula.getId());
		validarAvaliacaoInedita(participante.getId(), aula.getId());

		Avaliacao avaliacao = new Avaliacao();
		avaliacao.setParticipante(participante);
		avaliacao.setAula(aula);
		avaliacao.setNota(request.nota());
		avaliacao.setDidatica(request.didatica());
		avaliacao.setSugestao(request.sugestao());
		avaliacao.setNps(request.nps());

		if (NUMEROS_AULA_DESPERTAR_PARA_SERVIR.contains(aula.getTipoAula().getNumero())) {
			registrarDespertarParaServir(participante, avaliacao, request);
		}

		return AvaliacaoResponse.from(avaliacaoRepository.save(avaliacao));
	}

	/**
	 * Ultima resposta vale: sobrescreve tanto a avaliacao quanto a base do
	 * participante (write-back pelo CPF, Entrega 1).
	 */
	private void registrarDespertarParaServir(Participante participante, Avaliacao avaliacao, AvaliacaoRequest request) {
		String desejaServir = request.desejaServir() != null ? (request.desejaServir() ? "Sim" : "Nao") : null;
		avaliacao.setDesejaServir(desejaServir);
		avaliacao.setHabilidades(request.habilidades());

		if (desejaServir != null) {
			participante.setDesejaServir(desejaServir);
		}
		if (request.habilidades() != null && !request.habilidades().isBlank()) {
			participante.setHabilidades(request.habilidades());
		}
	}

	@Override
	@Transactional(readOnly = true)
	public List<AvaliacaoResponse> listarPorAula(UUID aulaId) {
		buscarAulaPorId(aulaId);
		return avaliacaoRepository.findByAulaId(aulaId).stream().map(AvaliacaoResponse::from).toList();
	}

	private Aula buscarAulaPorId(UUID aulaId) {
		return aulaRepository.findById(aulaId).orElseThrow(() -> new AulaNaoEncontradaException(aulaId));
	}

	private Participante buscarParticipantePorCpf(String cpf) {
		String cpfLimpo = cpf.replaceAll("\\D", "");
		return participanteRepository.findByCpf(cpfLimpo)
				.orElseThrow(() -> new ParticipanteNaoEncontradoException(cpfLimpo));
	}

	private void validarPresencaRegistrada(UUID participanteId, UUID aulaId) {
		if (!presencaRepository.existsByParticipanteIdAndAulaId(participanteId, aulaId)) {
			throw new CheckinNaoRealizadoException(participanteId, aulaId);
		}
	}

	private void validarAvaliacaoInedita(UUID participanteId, UUID aulaId) {
		if (avaliacaoRepository.existsByParticipanteIdAndAulaId(participanteId, aulaId)) {
			throw new AvaliacaoDuplicadaException(participanteId, aulaId);
		}
	}

}
