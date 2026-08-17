package br.com.familychurch.p3.aula.scheduler;

import br.com.familychurch.p3.aula.service.AulaService;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Fecha/abre aulas por data sozinho, sem depender de alguem acessar a tela
 * da turma (a sincronizacao lazy em AulaServiceImpl continua existindo como
 * reforco, mas nao e mais a unica forma de transicionar status). Roda logo
 * apos a meia-noite todo dia e tambem uma vez na subida da aplicacao, para
 * cobrir o caso do servidor ter ficado fora do ar durante a virada do dia.
 */
@Component
public class AulaStatusScheduler {

	private final AulaService aulaService;

	public AulaStatusScheduler(AulaService aulaService) {
		this.aulaService = aulaService;
	}

	@EventListener(ApplicationReadyEvent.class)
	public void aoSubirAplicacao() {
		aulaService.sincronizarStatusPorDataTodasTurmasAtivas();
	}

	@Scheduled(cron = "0 5 0 * * *")
	public void sincronizarDiariamente() {
		aulaService.sincronizarStatusPorDataTodasTurmasAtivas();
	}

}
