package br.com.familychurch.p3.aula.controller;

import br.com.familychurch.p3.aula.dto.AgendaRequest;
import br.com.familychurch.p3.aula.dto.AulaResponse;
import br.com.familychurch.p3.aula.service.AulaService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AulaController {

	private final AulaService aulaService;

	public AulaController(AulaService aulaService) {
		this.aulaService = aulaService;
	}

	@PutMapping("/api/turmas/{turmaId}/agenda")
	public List<AulaResponse> definirAgenda(@PathVariable UUID turmaId, @Valid @RequestBody AgendaRequest request) {
		return aulaService.definirAgenda(turmaId, request);
	}

	@GetMapping("/api/turmas/{turmaId}/aulas")
	public List<AulaResponse> listarPorTurma(@PathVariable UUID turmaId) {
		return aulaService.listarPorTurma(turmaId);
	}

	@GetMapping("/api/aulas/{id}")
	public AulaResponse buscarPorId(@PathVariable UUID id) {
		return aulaService.buscarPorId(id);
	}

	@GetMapping("/api/publico/turmas/{token}/aula-ativa")
	public AulaResponse buscarAulaAtivaPorToken(@PathVariable String token) {
		return aulaService.buscarAulaAtivaPorToken(token);
	}

}
