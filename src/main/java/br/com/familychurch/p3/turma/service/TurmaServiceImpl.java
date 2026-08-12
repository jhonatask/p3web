package br.com.familychurch.p3.turma.service;

import br.com.familychurch.p3.aluno.repository.AlunoRepository;
import br.com.familychurch.p3.aula.Aula;
import br.com.familychurch.p3.aula.repository.AulaRepository;
import br.com.familychurch.p3.avaliacao.repository.AvaliacaoRepository;
import br.com.familychurch.p3.curso.Curso;
import br.com.familychurch.p3.curso.exception.CursoNaoEncontradoException;
import br.com.familychurch.p3.curso.repository.CursoRepository;
import br.com.familychurch.p3.presenca.repository.PresencaRepository;
import br.com.familychurch.p3.turma.StatusTurma;
import br.com.familychurch.p3.turma.Turma;
import br.com.familychurch.p3.turma.dto.AulaDashboardItem;
import br.com.familychurch.p3.turma.dto.TurmaDashboardResponse;
import br.com.familychurch.p3.turma.dto.TurmaPublicaResponse;
import br.com.familychurch.p3.turma.dto.TurmaRequest;
import br.com.familychurch.p3.turma.dto.TurmaResponse;
import br.com.familychurch.p3.turma.exception.CursoInativoException;
import br.com.familychurch.p3.turma.exception.TurmaEncerradaException;
import br.com.familychurch.p3.turma.exception.TurmaNaoEncontradaException;
import br.com.familychurch.p3.turma.repository.TurmaRepository;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;
import javax.imageio.ImageIO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TurmaServiceImpl implements TurmaService {

	private static final String ALFABETO_TOKEN = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
	private static final int TAMANHO_TOKEN = 8;
	private static final int MAX_TENTATIVAS_TOKEN = 10;
	private static final int TAMANHO_IMAGEM_QRCODE = 300;

	private final TurmaRepository turmaRepository;
	private final CursoRepository cursoRepository;
	private final AulaRepository aulaRepository;
	private final PresencaRepository presencaRepository;
	private final AlunoRepository alunoRepository;
	private final AvaliacaoRepository avaliacaoRepository;
	private final String baseUrlCadastro;
	private final SecureRandom secureRandom = new SecureRandom();

	public TurmaServiceImpl(TurmaRepository turmaRepository, CursoRepository cursoRepository,
			AulaRepository aulaRepository, PresencaRepository presencaRepository, AlunoRepository alunoRepository,
			AvaliacaoRepository avaliacaoRepository, @Value("${app.cadastro.base-url}") String baseUrlCadastro) {
		this.turmaRepository = turmaRepository;
		this.cursoRepository = cursoRepository;
		this.aulaRepository = aulaRepository;
		this.presencaRepository = presencaRepository;
		this.alunoRepository = alunoRepository;
		this.avaliacaoRepository = avaliacaoRepository;
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

	@Override
	@Transactional(readOnly = true)
	public TurmaDashboardResponse obterDashboard(UUID turmaId) {
		Turma turma = buscarEntidadePorId(turmaId);
		long totalAlunos = alunoRepository.countByTurmaId(turmaId);
		List<Aula> aulas = aulaRepository.findByTurmaIdOrderByTipoAulaOrdemAsc(turmaId);

		List<AulaDashboardItem> porAula = aulas.stream()
				.map(aula -> montarItemDashboard(aula, totalAlunos))
				.toList();

		double frequenciaMedia = porAula.isEmpty() ? 0
				: porAula.stream().mapToDouble(AulaDashboardItem::percentualPresenca).average().orElse(0);
		Double notaMedia = mediaDasMedias(porAula.stream().map(AulaDashboardItem::notaMedia));
		Double npsMedia = mediaDasMedias(porAula.stream().map(AulaDashboardItem::npsMedia));

		return new TurmaDashboardResponse(
				turma.getId(),
				turma.getNome(),
				aulas.size(),
				(int) totalAlunos,
				frequenciaMedia,
				notaMedia,
				npsMedia,
				porAula);
	}

	@Override
	@Transactional(readOnly = true)
	public byte[] gerarImagemQrCode(UUID turmaId) {
		Turma turma = buscarEntidadePorId(turmaId);
		return gerarPng(TurmaResponse.from(turma, baseUrlCadastro).linkCadastro());
	}

	private AulaDashboardItem montarItemDashboard(Aula aula, long totalAlunos) {
		long totalPresencas = presencaRepository.countByAulaId(aula.getId());
		double percentualPresenca = totalAlunos == 0 ? 0 : (totalPresencas * 100.0 / totalAlunos);
		Double notaMedia = avaliacaoRepository.mediaNotaPorAula(aula.getId());
		Double npsMedia = avaliacaoRepository.mediaNpsPorAula(aula.getId());
		return new AulaDashboardItem(
				aula.getId(), aula.getTipoAula().getNumero(), aula.getData(), totalPresencas, percentualPresenca,
				notaMedia, npsMedia);
	}

	private Double mediaDasMedias(Stream<Double> medias) {
		List<Double> validas = medias.filter(Objects::nonNull).toList();
		if (validas.isEmpty()) {
			return null;
		}
		return validas.stream().mapToDouble(Double::doubleValue).average().orElse(0);
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

	private byte[] gerarPng(String conteudo) {
		try {
			BitMatrix matriz = new QRCodeWriter().encode(
					conteudo, BarcodeFormat.QR_CODE, TAMANHO_IMAGEM_QRCODE, TAMANHO_IMAGEM_QRCODE);
			BufferedImage imagem = new BufferedImage(
					matriz.getWidth(), matriz.getHeight(), BufferedImage.TYPE_INT_RGB);
			for (int x = 0; x < matriz.getWidth(); x++) {
				for (int y = 0; y < matriz.getHeight(); y++) {
					imagem.setRGB(x, y, matriz.get(x, y) ? 0x000000 : 0xFFFFFF);
				}
			}
			ByteArrayOutputStream saida = new ByteArrayOutputStream();
			ImageIO.write(imagem, "png", saida);
			return saida.toByteArray();
		} catch (WriterException | IOException exception) {
			throw new IllegalStateException("Falha ao gerar imagem do QR code", exception);
		}
	}

	private TurmaResponse toResponse(Turma turma) {
		return TurmaResponse.from(turma, baseUrlCadastro);
	}

}
