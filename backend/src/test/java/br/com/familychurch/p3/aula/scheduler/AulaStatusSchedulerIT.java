package br.com.familychurch.p3.aula.scheduler;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.familychurch.p3.IntegrationTestSupport;
import br.com.familychurch.p3.aluno.repository.AlunoRepository;
import br.com.familychurch.p3.aula.Aula;
import br.com.familychurch.p3.aula.StatusAula;
import br.com.familychurch.p3.aula.TipoAula;
import br.com.familychurch.p3.aula.repository.AulaRepository;
import br.com.familychurch.p3.aula.repository.TipoAulaRepository;
import br.com.familychurch.p3.aula.service.AulaService;
import br.com.familychurch.p3.avaliacao.repository.AvaliacaoRepository;
import br.com.familychurch.p3.curso.Curso;
import br.com.familychurch.p3.curso.repository.CursoRepository;
import br.com.familychurch.p3.presenca.repository.PresencaRepository;
import br.com.familychurch.p3.turma.StatusTurma;
import br.com.familychurch.p3.turma.Turma;
import br.com.familychurch.p3.turma.repository.TurmaRepository;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class AulaStatusSchedulerIT extends IntegrationTestSupport {

	@Autowired
	private AulaService aulaService;

	@Autowired
	private AulaRepository aulaRepository;

	@Autowired
	private TipoAulaRepository tipoAulaRepository;

	@Autowired
	private TurmaRepository turmaRepository;

	@Autowired
	private CursoRepository cursoRepository;

	@Autowired
	private AlunoRepository alunoRepository;

	@Autowired
	private PresencaRepository presencaRepository;

	@Autowired
	private AvaliacaoRepository avaliacaoRepository;

	@BeforeEach
	void configurarCenario() {
		avaliacaoRepository.deleteAll();
		presencaRepository.deleteAll();
		aulaRepository.deleteAll();
		alunoRepository.deleteAll();
		turmaRepository.deleteAll();
	}

	@Test
	void deveAbrirAulaAtrasadaSemNinguemTerAcessadoNoDiaCerto() {
		Turma turma = criarTurma(StatusTurma.ATIVA);
		Aula aula = criarAula(turma, LocalDate.now().minusDays(10), StatusAula.AGENDADA);

		aulaService.sincronizarStatusPorDataTodasTurmasAtivas();

		Aula recarregada = aulaRepository.findById(aula.getId()).orElseThrow();
		assertThat(recarregada.getStatus()).isEqualTo(StatusAula.ATIVA);
	}

	@Test
	void deveEncerrarAulaAtivaComDataNoPassado() {
		Turma turma = criarTurma(StatusTurma.ATIVA);
		Aula aula = criarAula(turma, LocalDate.now().minusDays(1), StatusAula.ATIVA);

		aulaService.sincronizarStatusPorDataTodasTurmasAtivas();

		Aula recarregada = aulaRepository.findById(aula.getId()).orElseThrow();
		assertThat(recarregada.getStatus()).isEqualTo(StatusAula.ENCERRADA);
	}

	@Test
	void naoDeveMexerEmAulaDeTurmaEncerrada() {
		Turma turma = criarTurma(StatusTurma.ENCERRADA);
		Aula aula = criarAula(turma, LocalDate.now().minusDays(10), StatusAula.AGENDADA);

		aulaService.sincronizarStatusPorDataTodasTurmasAtivas();

		Aula recarregada = aulaRepository.findById(aula.getId()).orElseThrow();
		assertThat(recarregada.getStatus()).isEqualTo(StatusAula.AGENDADA);
	}

	private Turma criarTurma(StatusTurma status) {
		Curso curso = cursoRepository.findById(obterCursoEscolaHombridade()).orElseThrow();

		Turma turma = new Turma();
		turma.setCurso(curso);
		turma.setNome("Campus 85 - Turma 13 - " + System.nanoTime());
		turma.setTokenCadastro("TK" + System.nanoTime() % 1_000_000);
		turma.setStatus(status);
		return turmaRepository.save(turma);
	}

	private Aula criarAula(Turma turma, LocalDate data, StatusAula status) {
		TipoAula tipoAula = tipoAulaRepository.findById(obterTipoAulaPorNumero(1)).orElseThrow();

		Aula aula = new Aula();
		aula.setTurma(turma);
		aula.setTipoAula(tipoAula);
		aula.setData(data);
		aula.setStatus(status);
		return aulaRepository.save(aula);
	}

}
