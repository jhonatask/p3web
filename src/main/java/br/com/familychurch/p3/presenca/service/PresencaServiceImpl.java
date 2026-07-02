package br.com.familychurch.p3.presenca.service;

import br.com.familychurch.p3.aluno.Aluno;
import br.com.familychurch.p3.aluno.repository.AlunoRepository;
import br.com.familychurch.p3.aula.Aula;
import br.com.familychurch.p3.aula.repository.AulaRepository;
import br.com.familychurch.p3.presenca.MetodoPresenca;
import br.com.familychurch.p3.presenca.Presenca;
import br.com.familychurch.p3.presenca.dto.CheckinRequest;
import br.com.familychurch.p3.presenca.dto.MarcarPresencaManualRequest;
import br.com.familychurch.p3.presenca.dto.PresencaResponse;
import br.com.familychurch.p3.presenca.exception.AlunoNaoEncontradoException;
import br.com.familychurch.p3.presenca.exception.AlunoNaoPertenceATurmaException;
import br.com.familychurch.p3.presenca.exception.AulaNaoEncontradaException;
import br.com.familychurch.p3.presenca.exception.JanelaAulaFechadaException;
import br.com.familychurch.p3.presenca.exception.PresencaDuplicadaException;
import br.com.familychurch.p3.presenca.exception.QrCodeInvalidoException;
import br.com.familychurch.p3.presenca.repository.PresencaRepository;
import br.com.familychurch.p3.usuarioadmin.repository.UsuarioAdminRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PresencaServiceImpl implements PresencaService {

	private final PresencaRepository presencaRepository;
	private final AulaRepository aulaRepository;
	private final AlunoRepository alunoRepository;
	private final UsuarioAdminRepository usuarioAdminRepository;
	private final SimpMessagingTemplate messagingTemplate;

	public PresencaServiceImpl(PresencaRepository presencaRepository, AulaRepository aulaRepository,
			AlunoRepository alunoRepository, UsuarioAdminRepository usuarioAdminRepository,
			SimpMessagingTemplate messagingTemplate) {
		this.presencaRepository = presencaRepository;
		this.aulaRepository = aulaRepository;
		this.alunoRepository = alunoRepository;
		this.usuarioAdminRepository = usuarioAdminRepository;
		this.messagingTemplate = messagingTemplate;
	}

	@Override
	public PresencaResponse checkin(CheckinRequest request) {
		Aula aula = aulaRepository.findByQrToken(request.qrToken()).orElseThrow(QrCodeInvalidoException::new);
		if (!aula.janelaAberta(LocalDateTime.now())) {
			throw new JanelaAulaFechadaException(aula.getId());
		}
		Aluno aluno = buscarAlunoDaTurma(request.cpf(), aula);
		validarPresencaInedita(aluno.getId(), aula.getId());

		Presenca presenca = new Presenca();
		presenca.setAluno(aluno);
		presenca.setAula(aula);
		presenca.setTimestampCheckin(LocalDateTime.now());
		presenca.setMetodo(MetodoPresenca.QRCODE);

		return salvarEPublicar(presenca);
	}

	@Override
	public PresencaResponse marcarManual(UUID aulaId, MarcarPresencaManualRequest request, UUID professorId) {
		Aula aula = buscarAulaPorId(aulaId);
		Aluno aluno = buscarAlunoDaTurma(request.cpf(), aula);
		validarPresencaInedita(aluno.getId(), aula.getId());

		Presenca presenca = new Presenca();
		presenca.setAluno(aluno);
		presenca.setAula(aula);
		presenca.setTimestampCheckin(LocalDateTime.now());
		presenca.setMetodo(MetodoPresenca.MANUAL);
		presenca.setValidadoPor(usuarioAdminRepository.getReferenceById(professorId));

		return salvarEPublicar(presenca);
	}

	@Override
	@Transactional(readOnly = true)
	public List<PresencaResponse> listarPorAula(UUID aulaId) {
		buscarAulaPorId(aulaId);
		return presencaRepository.findByAulaIdOrderByTimestampCheckinAsc(aulaId).stream()
				.map(PresencaResponse::from)
				.toList();
	}

	private Aula buscarAulaPorId(UUID aulaId) {
		return aulaRepository.findById(aulaId).orElseThrow(() -> new AulaNaoEncontradaException(aulaId));
	}

	private Aluno buscarAlunoDaTurma(String cpf, Aula aula) {
		String cpfLimpo = cpf.replaceAll("\\D", "");
		Aluno aluno = alunoRepository.findByCpf(cpfLimpo).orElseThrow(() -> new AlunoNaoEncontradoException(cpfLimpo));
		if (!aluno.getTurma().getId().equals(aula.getTurma().getId())) {
			throw new AlunoNaoPertenceATurmaException(aluno.getId(), aula.getId());
		}
		return aluno;
	}

	private void validarPresencaInedita(UUID alunoId, UUID aulaId) {
		if (presencaRepository.existsByAlunoIdAndAulaId(alunoId, aulaId)) {
			throw new PresencaDuplicadaException(alunoId, aulaId);
		}
	}

	private PresencaResponse salvarEPublicar(Presenca presenca) {
		PresencaResponse response = PresencaResponse.from(presencaRepository.save(presenca));
		messagingTemplate.convertAndSend("/topic/aulas/%s/presencas".formatted(response.aulaId()), response);
		return response;
	}

}
