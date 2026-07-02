package br.com.familychurch.p3.aula.service;

import br.com.familychurch.p3.aula.Aula;
import br.com.familychurch.p3.aula.dto.AulaRequest;
import br.com.familychurch.p3.aula.dto.AulaResponse;
import br.com.familychurch.p3.aula.exception.AulaNaoEncontradaException;
import br.com.familychurch.p3.aula.exception.JanelaInvalidaException;
import br.com.familychurch.p3.aula.exception.TurmaEncerradaException;
import br.com.familychurch.p3.aula.exception.TurmaNaoEncontradaException;
import br.com.familychurch.p3.aula.repository.AulaRepository;
import br.com.familychurch.p3.turma.StatusTurma;
import br.com.familychurch.p3.turma.Turma;
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
import java.util.UUID;
import javax.imageio.ImageIO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AulaServiceImpl implements AulaService {

	private static final String ALFABETO_TOKEN = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
	private static final int TAMANHO_TOKEN = 10;
	private static final int MAX_TENTATIVAS_TOKEN = 10;
	private static final int TAMANHO_IMAGEM_QRCODE = 300;

	private final AulaRepository aulaRepository;
	private final TurmaRepository turmaRepository;
	private final SecureRandom secureRandom = new SecureRandom();

	public AulaServiceImpl(AulaRepository aulaRepository, TurmaRepository turmaRepository) {
		this.aulaRepository = aulaRepository;
		this.turmaRepository = turmaRepository;
	}

	@Override
	public AulaResponse criar(UUID turmaId, AulaRequest request) {
		Turma turma = buscarTurmaAtiva(turmaId);
		if (!request.janelaFechamento().isAfter(request.janelaAbertura())) {
			throw new JanelaInvalidaException();
		}

		Aula aula = new Aula();
		aula.setTurma(turma);
		aula.setNumero(request.numero());
		aula.setModulo(request.modulo());
		aula.setData(request.data());
		aula.setJanelaAbertura(request.janelaAbertura());
		aula.setJanelaFechamento(request.janelaFechamento());
		aula.setQrToken(gerarQrTokenUnico());

		return AulaResponse.from(aulaRepository.save(aula));
	}

	@Override
	@Transactional(readOnly = true)
	public List<AulaResponse> listarPorTurma(UUID turmaId) {
		return aulaRepository.findByTurmaId(turmaId).stream().map(AulaResponse::from).toList();
	}

	@Override
	@Transactional(readOnly = true)
	public AulaResponse buscarPorId(UUID id) {
		return AulaResponse.from(buscarEntidadePorId(id));
	}

	@Override
	@Transactional(readOnly = true)
	public byte[] gerarImagemQrCode(UUID id) {
		Aula aula = buscarEntidadePorId(id);
		return gerarPng(aula.getQrToken());
	}

	private Turma buscarTurmaAtiva(UUID turmaId) {
		Turma turma = turmaRepository.findById(turmaId).orElseThrow(() -> new TurmaNaoEncontradaException(turmaId));
		if (turma.getStatus() == StatusTurma.ENCERRADA) {
			throw new TurmaEncerradaException(turmaId);
		}
		return turma;
	}

	private Aula buscarEntidadePorId(UUID id) {
		return aulaRepository.findById(id).orElseThrow(() -> new AulaNaoEncontradaException(id));
	}

	private String gerarQrTokenUnico() {
		for (int tentativa = 0; tentativa < MAX_TENTATIVAS_TOKEN; tentativa++) {
			String candidato = gerarToken();
			if (!aulaRepository.existsByQrToken(candidato)) {
				return candidato;
			}
		}
		throw new IllegalStateException("Nao foi possivel gerar um qr_token unico apos %d tentativas"
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

}
