package br.com.familychurch.p3.aluno.service;

import br.com.familychurch.p3.aluno.Aluno;
import br.com.familychurch.p3.aluno.dto.AlunoResponse;
import br.com.familychurch.p3.aluno.repository.AlunoRepository;
import br.com.familychurch.p3.participante.repository.ParticipanteRepository;
import br.com.familychurch.p3.turma.repository.TurmaRepository;
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

	public AlunoServiceImpl(AlunoRepository alunoRepository, ParticipanteRepository participanteRepository,
			TurmaRepository turmaRepository) {
		this.alunoRepository = alunoRepository;
		this.participanteRepository = participanteRepository;
		this.turmaRepository = turmaRepository;
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

}
