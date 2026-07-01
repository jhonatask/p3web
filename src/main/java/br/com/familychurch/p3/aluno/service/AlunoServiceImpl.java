package br.com.familychurch.p3.aluno.service;

import br.com.familychurch.p3.aluno.Aluno;
import br.com.familychurch.p3.aluno.dto.AlunoCadastroRequest;
import br.com.familychurch.p3.aluno.dto.AlunoCadastroResponse;
import br.com.familychurch.p3.aluno.dto.AlunoResponse;
import br.com.familychurch.p3.aluno.exception.AlunoJaCadastradoException;
import br.com.familychurch.p3.aluno.exception.ConsentimentoLgpdObrigatorioException;
import br.com.familychurch.p3.aluno.exception.TurmaEncerradaException;
import br.com.familychurch.p3.aluno.exception.TurmaNaoEncontradaException;
import br.com.familychurch.p3.aluno.repository.AlunoRepository;
import br.com.familychurch.p3.turma.StatusTurma;
import br.com.familychurch.p3.turma.Turma;
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
	private final TurmaRepository turmaRepository;

	public AlunoServiceImpl(AlunoRepository alunoRepository, TurmaRepository turmaRepository) {
		this.alunoRepository = alunoRepository;
		this.turmaRepository = turmaRepository;
	}

	@Override
	public AlunoCadastroResponse cadastrar(String tokenCadastro, AlunoCadastroRequest request) {
		Turma turma = buscarTurmaAtiva(tokenCadastro);
		String cpf = request.cpf().replaceAll("\\D", "");
		validarCpfDisponivel(cpf);
		validarConsentimentoLgpd(request.aceiteLgpd());

		Aluno aluno = new Aluno();
		aluno.setNome(request.nome());
		aluno.setCpf(cpf);
		aluno.setCelular(request.celular());
		aluno.setCampus(request.campus());
		aluno.setTurma(turma);
		aluno.setDataCadastro(LocalDateTime.now());
		aluno.setConsentimentoLgpd(true);
		aluno.setConsentimentoLgpdTimestamp(LocalDateTime.now());

		return AlunoCadastroResponse.from(alunoRepository.save(aluno));
	}

	@Override
	@Transactional(readOnly = true)
	public List<AlunoResponse> listarPorTurma(UUID turmaId) {
		return alunoRepository.findByTurmaId(turmaId).stream().map(AlunoResponse::from).toList();
	}

	private Turma buscarTurmaAtiva(String tokenCadastro) {
		Turma turma = turmaRepository.findByTokenCadastro(tokenCadastro)
				.orElseThrow(() -> new TurmaNaoEncontradaException(tokenCadastro));
		if (turma.getStatus() == StatusTurma.ENCERRADA) {
			throw new TurmaEncerradaException(tokenCadastro);
		}
		return turma;
	}

	private void validarCpfDisponivel(String cpf) {
		if (alunoRepository.existsByCpf(cpf)) {
			throw new AlunoJaCadastradoException(cpf);
		}
	}

	private void validarConsentimentoLgpd(boolean aceiteLgpd) {
		if (!aceiteLgpd) {
			throw new ConsentimentoLgpdObrigatorioException();
		}
	}

}
