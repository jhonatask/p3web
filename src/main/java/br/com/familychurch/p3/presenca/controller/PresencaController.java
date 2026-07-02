package br.com.familychurch.p3.presenca.controller;

import br.com.familychurch.p3.auth.UsuarioAdminDetails;
import br.com.familychurch.p3.presenca.dto.MarcarPresencaManualRequest;
import br.com.familychurch.p3.presenca.dto.PresencaResponse;
import br.com.familychurch.p3.presenca.service.PresencaService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PresencaController {

	private final PresencaService presencaService;

	public PresencaController(PresencaService presencaService) {
		this.presencaService = presencaService;
	}

	@GetMapping("/api/aulas/{aulaId}/presencas")
	public List<PresencaResponse> listarPorAula(@PathVariable UUID aulaId) {
		return presencaService.listarPorAula(aulaId);
	}

	@PostMapping("/api/aulas/{aulaId}/presencas/manual")
	public ResponseEntity<PresencaResponse> marcarManual(
			@PathVariable UUID aulaId,
			@Valid @RequestBody MarcarPresencaManualRequest request,
			@AuthenticationPrincipal UsuarioAdminDetails principal) {
		PresencaResponse response = presencaService.marcarManual(aulaId, request, principal.getUsuarioAdmin().getId());
		return ResponseEntity.status(201).body(response);
	}

}
