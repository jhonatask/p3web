package br.com.familychurch.p3.formatura.service;

import br.com.familychurch.p3.aluno.Aluno;
import br.com.familychurch.p3.aluno.repository.AlunoRepository;
import br.com.familychurch.p3.aula.ModuloAula;
import br.com.familychurch.p3.aula.TipoAula;
import br.com.familychurch.p3.aula.repository.TipoAulaRepository;
import br.com.familychurch.p3.formatura.dto.AulaFalta;
import br.com.familychurch.p3.formatura.dto.StatusFormatura;
import br.com.familychurch.p3.formatura.dto.StatusModulo;
import br.com.familychurch.p3.formatura.exception.MatriculaNaoEncontradaException;
import br.com.familychurch.p3.historico.HistoricoItem;
import br.com.familychurch.p3.historico.OrigemHistorico;
import br.com.familychurch.p3.historico.StatusHistorico;
import br.com.familychurch.p3.historico.TipoHistorico;
import br.com.familychurch.p3.historico.repository.HistoricoItemRepository;
import br.com.familychurch.p3.participante.repository.ParticipanteRepository;
import br.com.familychurch.p3.presenca.repository.PresencaRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class FormaturaServiceImpl implements FormaturaService {

	private static final String ITEM_ESCOLA_HOMBRIDADE = "Escola de Hombridade";
	private static final int MAXIMO_FALTAS_POR_MODULO = 1;

	private final AlunoRepository alunoRepository;
	private final TipoAulaRepository tipoAulaRepository;
	private final PresencaRepository presencaRepository;
	private final HistoricoItemRepository historicoItemRepository;
	private final ParticipanteRepository participanteRepository;

	public FormaturaServiceImpl(AlunoRepository alunoRepository, TipoAulaRepository tipoAulaRepository,
			PresencaRepository presencaRepository, HistoricoItemRepository historicoItemRepository,
			ParticipanteRepository participanteRepository) {
		this.alunoRepository = alunoRepository;
		this.tipoAulaRepository = tipoAulaRepository;
		this.presencaRepository = presencaRepository;
		this.historicoItemRepository = historicoItemRepository;
		this.participanteRepository = participanteRepository;
	}

	@Override
	@Transactional(readOnly = true)
	public StatusFormatura calcularStatus(UUID participanteId) {
		Aluno matricula = buscarMatricula(participanteId);
		return calcularStatus(participanteId, matricula);
	}

	private StatusFormatura calcularStatus(UUID participanteId, Aluno matricula) {
		List<TipoAula> catalogo = tipoAulaRepository
				.findByCursoIdOrderByOrdemAsc(matricula.getTurma().getCurso().getId());

		Map<ModuloAula, List<TipoAula>> porModulo = catalogo.stream()
				.filter(TipoAula::contaParaFormatura)
				.collect(Collectors.groupingBy(TipoAula::getModulo, LinkedHashMap::new, Collectors.toList()));

		List<StatusModulo> modulos = new ArrayList<>();
		List<AulaFalta> aRepor = new ArrayList<>();
		boolean apto = true;

		for (Map.Entry<ModuloAula, List<TipoAula>> entrada : porModulo.entrySet()) {
			List<AulaFalta> faltasDoModulo = entrada.getValue().stream()
					.filter(tipoAula -> !presencaRepository
							.existsByParticipanteIdAndAula_TipoAulaId(participanteId, tipoAula.getId()))
					.map(tipoAula -> new AulaFalta(tipoAula.getId(), tipoAula.getNumero(), tipoAula.getTema()))
					.toList();

			boolean reprovado = faltasDoModulo.size() > MAXIMO_FALTAS_POR_MODULO;
			if (reprovado) {
				apto = false;
			}
			aRepor.addAll(faltasDoModulo);
			modulos.add(new StatusModulo(entrada.getKey(), entrada.getValue().size(), faltasDoModulo, reprovado));
		}

		return new StatusFormatura(participanteId, modulos, apto, aRepor);
	}

	@Override
	public void recalcularEGravarConclusao(UUID participanteId) {
		StatusFormatura status = calcularStatus(participanteId);
		if (!status.apto()) {
			return;
		}

		Aluno matricula = buscarMatricula(participanteId);
		HistoricoItem item = historicoItemRepository
				.findByParticipanteIdAndItemAndTipo(participanteId, ITEM_ESCOLA_HOMBRIDADE, TipoHistorico.CURSO)
				.orElseGet(() -> {
					HistoricoItem novo = new HistoricoItem();
					novo.setParticipante(participanteRepository.getReferenceById(participanteId));
					novo.setItem(ITEM_ESCOLA_HOMBRIDADE);
					novo.setTipo(TipoHistorico.CURSO);
					return novo;
				});
		item.setTurma(matricula.getTurma().getNome());
		item.setStatus(StatusHistorico.CONCLUIDO);
		item.setData(LocalDate.now());
		// A conclusao confirmada pela chamada e mais autoritativa que uma
		// autodeclaracao do cadastro base - CHAMADA prevalece mesmo ao
		// atualizar uma linha que originalmente veio de origem=CADASTRO.
		item.setOrigem(OrigemHistorico.CHAMADA);
		historicoItemRepository.save(item);
	}

	private Aluno buscarMatricula(UUID participanteId) {
		return alunoRepository.findByParticipanteId(participanteId)
				.orElseThrow(() -> new MatriculaNaoEncontradaException(participanteId));
	}

}
