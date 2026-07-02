package br.com.familychurch.p3.interesse.controller;

import br.com.familychurch.p3.interesse.dto.InteresseRequest;
import br.com.familychurch.p3.interesse.dto.InteresseResponse;
import br.com.familychurch.p3.interesse.service.InteresseService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class InteresseController {

	private final InteresseService interesseService;

	public InteresseController(InteresseService interesseService) {
		this.interesseService = interesseService;
	}

	@PostMapping("/api/publico/aulas/{aulaId}/interesses")
	public ResponseEntity<InteresseResponse> registrar(
			@PathVariable UUID aulaId, @Valid @RequestBody InteresseRequest request) {
		return ResponseEntity.status(201).body(interesseService.registrar(aulaId, request));
	}

	@GetMapping("/api/turmas/{turmaId}/interesses")
	public List<InteresseResponse> listarPorTurma(@PathVariable UUID turmaId) {
		return interesseService.listarPorTurma(turmaId);
	}

}
