package br.com.familychurch.p3.formatura.controller;

import br.com.familychurch.p3.formatura.dto.LinkWhatsappResponse;
import br.com.familychurch.p3.formatura.dto.PainelTurmaResponse;
import br.com.familychurch.p3.formatura.service.FormaturaService;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FormaturaController {

	private final FormaturaService formaturaService;

	public FormaturaController(FormaturaService formaturaService) {
		this.formaturaService = formaturaService;
	}

	@GetMapping("/api/turmas/{turmaId}/painel")
	public PainelTurmaResponse obterPainel(@PathVariable UUID turmaId) {
		return formaturaService.obterPainel(turmaId);
	}

	@GetMapping("/api/turmas/{turmaId}/painel/{participanteId}/link-whatsapp")
	public LinkWhatsappResponse gerarLinkWhatsapp(@PathVariable UUID turmaId, @PathVariable UUID participanteId) {
		return formaturaService.gerarLinkWhatsapp(turmaId, participanteId);
	}

}
