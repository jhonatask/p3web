package br.com.familychurch.p3.aula.service;

import br.com.familychurch.p3.aula.Aula;
import br.com.familychurch.p3.aula.StatusAula;
import br.com.familychurch.p3.aula.TipoAula;
import br.com.familychurch.p3.aula.dto.AgendaItemRequest;
import br.com.familychurch.p3.aula.dto.AgendaRequest;
import br.com.familychurch.p3.aula.dto.AulaResponse;
import br.com.familychurch.p3.aula.exception.AulaJaEncerradaException;
import br.com.familychurch.p3.aula.exception.AulaNaoEncontradaException;
import br.com.familychurch.p3.aula.exception.NenhumaAulaAtivaException;
import br.com.familychurch.p3.aula.exception.TipoAulaNaoEncontradoException;
import br.com.familychurch.p3.aula.exception.TipoAulaNaoPertenceAoCursoException;
import br.com.familychurch.p3.aula.exception.TurmaEncerradaException;
import br.com.familychurch.p3.aula.exception.TurmaNaoEncontradaException;
import br.com.familychurch.p3.aula.repository.AulaRepository;
import br.com.familychurch.p3.aula.repository.TipoAulaRepository;
import br.com.familychurch.p3.turma.StatusTurma;
import br.com.familychurch.p3.turma.Turma;
import br.com.familychurch.p3.turma.repository.TurmaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AulaServiceImpl implements AulaService {

	private final AulaRepository aulaRepository;
	private final TurmaRepository turmaRepository;
	private final TipoAulaRepository tipoAulaRepository;

	public AulaServiceImpl(AulaRepository aulaRepository, TurmaRepository turmaRepository,
			TipoAulaRepository tipoAulaRepository) {
		this.aulaRepository = aulaRepository;
		this.turmaRepository = turmaRepository;
		this.tipoAulaRepository = tipoAulaRepository;
	}

	@Override
	public List<AulaResponse> definirAgenda(UUID turmaId, AgendaRequest request) {
		Turma turma = buscarTurmaAtiva(turmaId);
		for (AgendaItemRequest item : request.itens()) {
			aplicarItemAgenda(turma, item);
		}
		sincronizarStatusPorData(turmaId);
		return listarPorTurma(turmaId);
	}

	private void aplicarItemAgenda(Turma turma, AgendaItemRequest item) {
		TipoAula tipoAula = tipoAulaRepository.findById(item.tipoAulaId())
				.orElseThrow(() -> new TipoAulaNaoEncontradoException(item.tipoAulaId()));
		if (!tipoAula.getCurso().getId().equals(turma.getCurso().getId())) {
			throw new TipoAulaNaoPertenceAoCursoException(tipoAula.getId(), turma.getCurso().getId());
		}

		Aula aula = aulaRepository.findByTurmaIdAndTipoAulaId(turma.getId(), tipoAula.getId())
				.orElseGet(() -> {
					Aula nova = new Aula();
					nova.setTurma(turma);
					nova.setTipoAula(tipoAula);
					return nova;
				});
		if (aula.getId() != null && aula.getStatus() == StatusAula.ENCERRADA) {
			throw new AulaJaEncerradaException(tipoAula.getId());
		}
		aula.setData(item.data());
		aulaRepository.save(aula);
	}

	@Override
	@Transactional(readOnly = true)
	public List<AulaResponse> listarPorTurma(UUID turmaId) {
		return aulaRepository.findByTurmaIdOrderByTipoAulaOrdemAsc(turmaId).stream().map(AulaResponse::from).toList();
	}

	@Override
	@Transactional(readOnly = true)
	public AulaResponse buscarPorId(UUID id) {
		return AulaResponse.from(buscarEntidadePorId(id));
	}

	@Override
	public AulaResponse buscarAulaAtiva(UUID turmaId) {
		sincronizarStatusPorData(turmaId);
		List<Aula> ativas = aulaRepository.findByTurmaIdAndStatusOrderByTipoAulaOrdemAsc(turmaId, StatusAula.ATIVA);
		if (ativas.isEmpty()) {
			throw new NenhumaAulaAtivaException(turmaId);
		}
		return AulaResponse.from(ativas.get(0));
	}

	@Override
	public AulaResponse buscarAulaAtivaPorToken(String tokenTurma) {
		Turma turma = turmaRepository.findByTokenCadastro(tokenTurma)
				.orElseThrow(() -> new TurmaNaoEncontradaException(tokenTurma));
		if (turma.getStatus() == StatusTurma.ENCERRADA) {
			throw new TurmaEncerradaException(tokenTurma);
		}
		return buscarAulaAtiva(turma.getId());
	}

	@Override
	public Optional<AulaResponse> encerrarAulaAtual(UUID turmaId) {
		sincronizarStatusPorData(turmaId);
		List<Aula> ativas = aulaRepository.findByTurmaIdAndStatusOrderByTipoAulaOrdemAsc(turmaId, StatusAula.ATIVA);
		if (ativas.isEmpty()) {
			throw new NenhumaAulaAtivaException(turmaId);
		}
		for (Aula ativa : ativas) {
			ativa.setStatus(StatusAula.ENCERRADA);
			ativa.setEncerradaManualmente(true);
		}

		return aulaRepository.findByTurmaIdOrderByTipoAulaOrdemAsc(turmaId).stream()
				.filter(aula -> aula.getStatus() == StatusAula.AGENDADA)
				.findFirst()
				.map(proxima -> {
					proxima.setStatus(StatusAula.ATIVA);
					return AulaResponse.from(proxima);
				});
	}

	@Override
	public void desativarAulaAtual(UUID turmaId) {
		sincronizarStatusPorData(turmaId);
		List<Aula> ativas = aulaRepository.findByTurmaIdAndStatusOrderByTipoAulaOrdemAsc(turmaId, StatusAula.ATIVA);
		if (ativas.isEmpty()) {
			throw new NenhumaAulaAtivaException(turmaId);
		}
		for (Aula ativa : ativas) {
			ativa.setStatus(StatusAula.ENCERRADA);
			ativa.setEncerradaManualmente(true);
		}
	}

	@Override
	public void sincronizarStatusPorData(UUID turmaId) {
		LocalDate hoje = LocalDate.now();
		for (Aula aula : aulaRepository.findByTurmaId(turmaId)) {
			if (aula.getStatus() == StatusAula.AGENDADA && aula.getData().isEqual(hoje)) {
				aula.setStatus(StatusAula.ATIVA);
			} else if (aula.getStatus() == StatusAula.ATIVA && aula.getData().isBefore(hoje)) {
				aula.setStatus(StatusAula.ENCERRADA);
			}
		}
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

}
