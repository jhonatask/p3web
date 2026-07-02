package br.com.familychurch.p3.interesse.service;

import br.com.familychurch.p3.aluno.Aluno;
import br.com.familychurch.p3.aluno.repository.AlunoRepository;
import br.com.familychurch.p3.aula.Aula;
import br.com.familychurch.p3.aula.repository.AulaRepository;
import br.com.familychurch.p3.interesse.Interesse;
import br.com.familychurch.p3.interesse.dto.InteresseRequest;
import br.com.familychurch.p3.interesse.dto.InteresseResponse;
import br.com.familychurch.p3.interesse.exception.AlunoNaoEncontradoException;
import br.com.familychurch.p3.interesse.exception.AlunoNaoPertenceATurmaException;
import br.com.familychurch.p3.interesse.exception.AulaNaoEncontradaException;
import br.com.familychurch.p3.interesse.exception.CheckinNaoRealizadoException;
import br.com.familychurch.p3.interesse.exception.InteresseDuplicadoException;
import br.com.familychurch.p3.interesse.repository.InteresseRepository;
import br.com.familychurch.p3.presenca.repository.PresencaRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class InteresseServiceImpl implements InteresseService {

	private final InteresseRepository interesseRepository;
	private final AulaRepository aulaRepository;
	private final AlunoRepository alunoRepository;
	private final PresencaRepository presencaRepository;

	public InteresseServiceImpl(InteresseRepository interesseRepository, AulaRepository aulaRepository,
			AlunoRepository alunoRepository, PresencaRepository presencaRepository) {
		this.interesseRepository = interesseRepository;
		this.aulaRepository = aulaRepository;
		this.alunoRepository = alunoRepository;
		this.presencaRepository = presencaRepository;
	}

	@Override
	public InteresseResponse registrar(UUID aulaId, InteresseRequest request) {
		Aula aula = buscarAulaPorId(aulaId);
		Aluno aluno = buscarAlunoDaTurma(request.cpf(), aula);
		validarCheckinRealizado(aluno.getId(), aula.getId());
		validarInteresseInedito(aluno.getId(), aula.getId());

		Interesse interesse = new Interesse();
		interesse.setAluno(aluno);
		interesse.setAula(aula);
		interesse.setGcHomens(request.gcHomens());
		interesse.setProximoAwake(request.proximoAwake());
		interesse.setDiscipulado(request.discipulado());

		return InteresseResponse.from(interesseRepository.save(interesse));
	}

	@Override
	@Transactional(readOnly = true)
	public List<InteresseResponse> listarPorTurma(UUID turmaId) {
		return interesseRepository.findByAulaTurmaId(turmaId).stream().map(InteresseResponse::from).toList();
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

	private void validarCheckinRealizado(UUID alunoId, UUID aulaId) {
		if (!presencaRepository.existsByAlunoIdAndAulaId(alunoId, aulaId)) {
			throw new CheckinNaoRealizadoException(alunoId, aulaId);
		}
	}

	private void validarInteresseInedito(UUID alunoId, UUID aulaId) {
		if (interesseRepository.existsByAlunoIdAndAulaId(alunoId, aulaId)) {
			throw new InteresseDuplicadoException(alunoId, aulaId);
		}
	}

}
