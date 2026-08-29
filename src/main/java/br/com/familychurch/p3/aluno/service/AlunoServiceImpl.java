package br.com.familychurch.p3.aluno.service;

import br.com.familychurch.p3.aluno.Aluno;
import br.com.familychurch.p3.aluno.dto.AlunoResponse;
import br.com.familychurch.p3.aluno.dto.MatriculaResponse;
import br.com.familychurch.p3.aluno.exception.TokenMatriculaInvalidoException;
import br.com.familychurch.p3.aluno.exception.TurmaNaoAceitaMatriculaException;
import br.com.familychurch.p3.aluno.repository.AlunoRepository;
import br.com.familychurch.p3.participante.Participante;
import br.com.familychurch.p3.participante.exception.ParticipanteNaoEncontradoException;
import br.com.familychurch.p3.participante.repository.ParticipanteRepository;
import br.com.familychurch.p3.turma.repository.TurmaRepository;
import br.com.familychurch.p3.turma.service.TurmaService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AlunoServiceImpl implements AlunoService {

	private final AlunoRepository alunoRepository;
	private final ParticipanteRepository participanteRepository;
	private final TurmaRepository turmaRepository;
	private final TurmaService turmaService;

	public AlunoServiceImpl(AlunoRepository alunoRepository, ParticipanteRepository participanteRepository,
			TurmaRepository turmaRepository, TurmaService turmaService) {
		this.alunoRepository = alunoRepository;
		this.participanteRepository = participanteRepository;
		this.turmaRepository = turmaRepository;
		this.turmaService = turmaService;
	}

	@Override
	public Aluno matricularSeNecessario(UUID participanteId, UUID turmaId) {
		return alunoRepository.findByParticipanteId(participanteId).orElseGet(() -> {
			Aluno novo = new Aluno();
			novo.setParticipante(participanteRepository.getReferenceById(participanteId));
			novo.setTurma(turmaRepository.getReferenceById(turmaId));
			novo.setDataMatricula(LocalDateTime.now());
			return alunoRepository.save(novo);
		});
	}

	@Override
	@Transactional(readOnly = true)
	public List<AlunoResponse> listarPorTurma(UUID turmaId) {
		return alunoRepository.findByTurmaId(turmaId).stream().map(AlunoResponse::from).toList();
	}

	@Override
	public MatriculaResponse matricularDireto(String cpf, String token, UUID turmaId) {
		String cpfLimpo = cpf.replaceAll("\\D", "");
		Participante participante = participanteRepository.findByCpf(cpfLimpo)
				.orElseThrow(() -> new ParticipanteNaoEncontradoException(cpfLimpo));
		validarTokenMatricula(participante, token);
		if (!turmaService.aceitaMatriculaDireta(turmaId)) {
			throw new TurmaNaoAceitaMatriculaException(turmaId);
		}

		boolean jaEstavaMatriculado = alunoRepository.findByParticipanteId(participante.getId()).isPresent();
		Aluno matricula = matricularSeNecessario(participante.getId(), turmaId);
		return new MatriculaResponse(matricula.getTurma().getId(), matricula.getTurma().getNome(), jaEstavaMatriculado);
	}

	private void validarTokenMatricula(Participante participante, String token) {
		String tokenEsperado = participante.getTokenMatricula();
		LocalDateTime expiraEm = participante.getTokenMatriculaExpiraEm();
		boolean valido = tokenEsperado != null && tokenEsperado.equals(token)
				&& expiraEm != null && expiraEm.isAfter(LocalDateTime.now());
		if (!valido) {
			throw new TokenMatriculaInvalidoException();
		}
	}

}
