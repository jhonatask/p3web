package br.com.familychurch.p3.avaliacao.service;

import br.com.familychurch.p3.aluno.Aluno;
import br.com.familychurch.p3.aluno.repository.AlunoRepository;
import br.com.familychurch.p3.aula.Aula;
import br.com.familychurch.p3.aula.repository.AulaRepository;
import br.com.familychurch.p3.avaliacao.Avaliacao;
import br.com.familychurch.p3.avaliacao.dto.AvaliacaoRequest;
import br.com.familychurch.p3.avaliacao.dto.AvaliacaoResponse;
import br.com.familychurch.p3.avaliacao.exception.AlunoNaoEncontradoException;
import br.com.familychurch.p3.avaliacao.exception.AlunoNaoPertenceATurmaException;
import br.com.familychurch.p3.avaliacao.exception.AulaNaoEncontradaException;
import br.com.familychurch.p3.avaliacao.exception.AvaliacaoDuplicadaException;
import br.com.familychurch.p3.avaliacao.exception.CheckinNaoRealizadoException;
import br.com.familychurch.p3.avaliacao.repository.AvaliacaoRepository;
import br.com.familychurch.p3.presenca.repository.PresencaRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AvaliacaoServiceImpl implements AvaliacaoService {

	private final AvaliacaoRepository avaliacaoRepository;
	private final AulaRepository aulaRepository;
	private final AlunoRepository alunoRepository;
	private final PresencaRepository presencaRepository;

	public AvaliacaoServiceImpl(AvaliacaoRepository avaliacaoRepository, AulaRepository aulaRepository,
			AlunoRepository alunoRepository, PresencaRepository presencaRepository) {
		this.avaliacaoRepository = avaliacaoRepository;
		this.aulaRepository = aulaRepository;
		this.alunoRepository = alunoRepository;
		this.presencaRepository = presencaRepository;
	}

	@Override
	public AvaliacaoResponse registrar(UUID aulaId, AvaliacaoRequest request) {
		Aula aula = buscarAulaPorId(aulaId);
		Aluno aluno = buscarAlunoDaTurma(request.cpf(), aula);
		validarCheckinRealizado(aluno.getId(), aula.getId());
		validarAvaliacaoInedita(aluno.getId(), aula.getId());

		Avaliacao avaliacao = new Avaliacao();
		avaliacao.setAluno(aluno);
		avaliacao.setAula(aula);
		avaliacao.setNota(request.nota());
		avaliacao.setDidatica(request.didatica());
		avaliacao.setSugestao(request.sugestao());
		avaliacao.setNps(request.nps());

		return AvaliacaoResponse.from(avaliacaoRepository.save(avaliacao));
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

	private void validarAvaliacaoInedita(UUID alunoId, UUID aulaId) {
		if (avaliacaoRepository.existsByAlunoIdAndAulaId(alunoId, aulaId)) {
			throw new AvaliacaoDuplicadaException(alunoId, aulaId);
		}
	}

}
