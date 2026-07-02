package br.com.familychurch.p3.turma.service;

import br.com.familychurch.p3.curso.Curso;
import br.com.familychurch.p3.curso.exception.CursoNaoEncontradoException;
import br.com.familychurch.p3.curso.repository.CursoRepository;
import br.com.familychurch.p3.turma.StatusTurma;
import br.com.familychurch.p3.turma.Turma;
import br.com.familychurch.p3.turma.dto.TurmaPublicaResponse;
import br.com.familychurch.p3.turma.dto.TurmaRequest;
import br.com.familychurch.p3.turma.dto.TurmaResponse;
import br.com.familychurch.p3.turma.exception.CursoInativoException;
import br.com.familychurch.p3.turma.exception.TurmaEncerradaException;
import br.com.familychurch.p3.turma.exception.TurmaNaoEncontradaException;
import br.com.familychurch.p3.turma.repository.TurmaRepository;
import java.security.SecureRandom;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TurmaServiceImpl implements TurmaService {

	private static final String ALFABETO_TOKEN = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
	private static final int TAMANHO_TOKEN = 8;
	private static final int MAX_TENTATIVAS_TOKEN = 10;

	private final TurmaRepository turmaRepository;
	private final CursoRepository cursoRepository;
	private final String baseUrlCadastro;
	private final SecureRandom secureRandom = new SecureRandom();

	public TurmaServiceImpl(TurmaRepository turmaRepository, CursoRepository cursoRepository,
			@Value("${app.cadastro.base-url}") String baseUrlCadastro) {
		this.turmaRepository = turmaRepository;
		this.cursoRepository = cursoRepository;
		this.baseUrlCadastro = baseUrlCadastro;
	}

	@Override
	public TurmaResponse criar(TurmaRequest request) {
		Curso curso = buscarCursoAtivo(request.cursoId());

		Turma turma = new Turma();
		turma.setCurso(curso);
		turma.setNome(request.nome());
		turma.setModuloAtual(request.moduloAtual());
		turma.setTokenCadastro(gerarTokenCadastroUnico());
		turma.setStatus(StatusTurma.ATIVA);

		return toResponse(turmaRepository.save(turma));
	}

	@Override
	@Transactional(readOnly = true)
	public List<TurmaResponse> listar(UUID cursoId, StatusTurma status) {
		List<Turma> turmas;
		if (cursoId != null && status != null) {
			turmas = turmaRepository.findByCursoIdAndStatus(cursoId, status);
		} else if (cursoId != null) {
			turmas = turmaRepository.findByCursoId(cursoId);
		} else if (status != null) {
			turmas = turmaRepository.findByStatus(status);
		} else {
			turmas = turmaRepository.findAll();
		}
		return turmas.stream().map(this::toResponse).toList();
	}

	@Override
	@Transactional(readOnly = true)
	public TurmaResponse buscarPorId(UUID id) {
		return toResponse(buscarEntidadePorId(id));
	}

	@Override
	public TurmaResponse encerrar(UUID id) {
		Turma turma = buscarEntidadePorId(id);
		turma.setStatus(StatusTurma.ENCERRADA);
		return toResponse(turma);
	}

	@Override
	@Transactional(readOnly = true)
	public TurmaPublicaResponse buscarPorTokenCadastro(String token) {
		Turma turma = turmaRepository.findByTokenCadastro(token)
				.orElseThrow(() -> new TurmaNaoEncontradaException(token));
		if (turma.getStatus() == StatusTurma.ENCERRADA) {
			throw new TurmaEncerradaException(token);
		}
		return TurmaPublicaResponse.from(turma);
	}

	private Curso buscarCursoAtivo(UUID cursoId) {
		Curso curso = cursoRepository.findById(cursoId).orElseThrow(() -> new CursoNaoEncontradoException(cursoId));
		if (!curso.isAtivo()) {
			throw new CursoInativoException(cursoId);
		}
		return curso;
	}

	private Turma buscarEntidadePorId(UUID id) {
		return turmaRepository.findById(id).orElseThrow(() -> new TurmaNaoEncontradaException(id));
	}

	private String gerarTokenCadastroUnico() {
		for (int tentativa = 0; tentativa < MAX_TENTATIVAS_TOKEN; tentativa++) {
			String candidato = gerarToken();
			if (!turmaRepository.existsByTokenCadastro(candidato)) {
				return candidato;
			}
		}
		throw new IllegalStateException("Nao foi possivel gerar um token de cadastro unico apos %d tentativas"
				.formatted(MAX_TENTATIVAS_TOKEN));
	}

	private String gerarToken() {
		StringBuilder token = new StringBuilder(TAMANHO_TOKEN);
		for (int i = 0; i < TAMANHO_TOKEN; i++) {
			token.append(ALFABETO_TOKEN.charAt(secureRandom.nextInt(ALFABETO_TOKEN.length())));
		}
		return token.toString();
	}

	private TurmaResponse toResponse(Turma turma) {
		return TurmaResponse.from(turma, baseUrlCadastro);
	}

}
