package br.com.familychurch.p3.presenca.service;

import br.com.familychurch.p3.aluno.Aluno;
import br.com.familychurch.p3.aluno.service.AlunoService;
import br.com.familychurch.p3.aula.Aula;
import br.com.familychurch.p3.aula.dto.AulaResponse;
import br.com.familychurch.p3.aula.repository.AulaRepository;
import br.com.familychurch.p3.aula.service.AulaService;
import br.com.familychurch.p3.formatura.service.FormaturaService;
import br.com.familychurch.p3.participante.Participante;
import br.com.familychurch.p3.participante.exception.ParticipanteNaoEncontradoException;
import br.com.familychurch.p3.participante.repository.ParticipanteRepository;
import br.com.familychurch.p3.presenca.MetodoPresenca;
import br.com.familychurch.p3.presenca.OrigemPresenca;
import br.com.familychurch.p3.presenca.Presenca;
import br.com.familychurch.p3.presenca.dto.ChamadaRequest;
import br.com.familychurch.p3.presenca.dto.MarcarPresencaManualRequest;
import br.com.familychurch.p3.presenca.dto.PresencaResponse;
import br.com.familychurch.p3.presenca.exception.AulaNaoEncontradaException;
import br.com.familychurch.p3.presenca.exception.PresencaDuplicadaException;
import br.com.familychurch.p3.presenca.repository.PresencaRepository;
import br.com.familychurch.p3.turma.Turma;
import br.com.familychurch.p3.turma.repository.TurmaRepository;
import br.com.familychurch.p3.usuarioadmin.repository.UsuarioAdminRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PresencaServiceImpl implements PresencaService {

	private final PresencaRepository presencaRepository;
	private final AulaRepository aulaRepository;
	private final TurmaRepository turmaRepository;
	private final ParticipanteRepository participanteRepository;
	private final UsuarioAdminRepository usuarioAdminRepository;
	private final AulaService aulaService;
	private final AlunoService alunoService;
	private final FormaturaService formaturaService;
	private final SimpMessagingTemplate messagingTemplate;

	public PresencaServiceImpl(PresencaRepository presencaRepository, AulaRepository aulaRepository,
			TurmaRepository turmaRepository, ParticipanteRepository participanteRepository,
			UsuarioAdminRepository usuarioAdminRepository, AulaService aulaService, AlunoService alunoService,
			FormaturaService formaturaService, SimpMessagingTemplate messagingTemplate) {
		this.presencaRepository = presencaRepository;
		this.aulaRepository = aulaRepository;
		this.turmaRepository = turmaRepository;
		this.participanteRepository = participanteRepository;
		this.usuarioAdminRepository = usuarioAdminRepository;
		this.aulaService = aulaService;
		this.alunoService = alunoService;
		this.formaturaService = formaturaService;
		this.messagingTemplate = messagingTemplate;
	}

	@Override
	public PresencaResponse registrarChamada(String tokenTurma, ChamadaRequest request) {
		AulaResponse aulaAtiva = aulaService.buscarAulaAtivaPorToken(tokenTurma);
		Participante participante = buscarParticipantePorCpf(request.cpf());
		Aula aula = aulaRepository.getReferenceById(aulaAtiva.id());
		Turma turmaDaSessao = turmaRepository.getReferenceById(aulaAtiva.turmaId());
		Aluno matricula = alunoService.matricularSeNecessario(participante.getId(), turmaDaSessao.getId());

		Optional<Presenca> existente = presencaRepository.findByParticipanteIdAndAulaId(participante.getId(), aula.getId());
		if (existente.isPresent()) {
			return PresencaResponse.from(existente.get(), matricula.getTurma().getNome());
		}

		OrigemPresenca origem = calcularOrigem(matricula, turmaDaSessao.getId());

		Presenca presenca = new Presenca();
		presenca.setParticipante(participante);
		presenca.setTurma(turmaDaSessao);
		presenca.setAula(aula);
		presenca.setTimestampCheckin(LocalDateTime.now());
		presenca.setMetodo(MetodoPresenca.FORMULARIO);
		presenca.setOrigem(origem);

		return salvarEPublicar(presenca, matricula.getTurma().getNome());
	}

	@Override
	public PresencaResponse marcarManual(UUID aulaId, MarcarPresencaManualRequest request, UUID professorId) {
		Aula aula = buscarAulaPorId(aulaId);
		Participante participante = buscarParticipantePorCpf(request.cpf());
		validarPresencaInedita(participante.getId(), aula.getId());
		Aluno matricula = alunoService.matricularSeNecessario(participante.getId(), aula.getTurma().getId());
		OrigemPresenca origem = calcularOrigem(matricula, aula.getTurma().getId());

		Presenca presenca = new Presenca();
		presenca.setParticipante(participante);
		presenca.setTurma(aula.getTurma());
		presenca.setAula(aula);
		presenca.setTimestampCheckin(LocalDateTime.now());
		presenca.setMetodo(MetodoPresenca.MANUAL);
		presenca.setOrigem(origem);
		presenca.setValidadoPor(usuarioAdminRepository.getReferenceById(professorId));

		return salvarEPublicar(presenca, matricula.getTurma().getNome());
	}

	@Override
	@Transactional(readOnly = true)
	public List<PresencaResponse> listarPorAula(UUID aulaId) {
		buscarAulaPorId(aulaId);
		return presencaRepository.findByAulaIdOrderByTimestampCheckinAsc(aulaId).stream()
				.map(PresencaResponse::from)
				.toList();
	}

	private OrigemPresenca calcularOrigem(Aluno matricula, UUID turmaDaSessaoId) {
		return matricula.getTurma().getId().equals(turmaDaSessaoId) ? OrigemPresenca.CHAMADA : OrigemPresenca.REPOSICAO;
	}

	private Aula buscarAulaPorId(UUID aulaId) {
		return aulaRepository.findById(aulaId).orElseThrow(() -> new AulaNaoEncontradaException(aulaId));
	}

	private Participante buscarParticipantePorCpf(String cpf) {
		String cpfLimpo = cpf.replaceAll("\\D", "");
		return participanteRepository.findByCpf(cpfLimpo)
				.orElseThrow(() -> new ParticipanteNaoEncontradoException(cpfLimpo));
	}

	private void validarPresencaInedita(UUID participanteId, UUID aulaId) {
		if (presencaRepository.existsByParticipanteIdAndAulaId(participanteId, aulaId)) {
			throw new PresencaDuplicadaException(participanteId, aulaId);
		}
	}

	private PresencaResponse salvarEPublicar(Presenca presenca, String turmaOrigemNome) {
		PresencaResponse response = PresencaResponse.from(presencaRepository.save(presenca), turmaOrigemNome);
		formaturaService.recalcularEGravarConclusao(presenca.getParticipante().getId());
		messagingTemplate.convertAndSend("/topic/aulas/%s/presencas".formatted(response.aulaId()), response);
		return response;
	}

}
