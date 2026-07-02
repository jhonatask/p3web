package br.com.familychurch.p3.aula.controller;

import br.com.familychurch.p3.aula.dto.AulaRequest;
import br.com.familychurch.p3.aula.dto.AulaResponse;
import br.com.familychurch.p3.aula.service.AulaService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
public class AulaController {

	private final AulaService aulaService;

	public AulaController(AulaService aulaService) {
		this.aulaService = aulaService;
	}

	@PostMapping("/api/turmas/{turmaId}/aulas")
	public ResponseEntity<AulaResponse> criar(@PathVariable UUID turmaId, @Valid @RequestBody AulaRequest request) {
		AulaResponse response = aulaService.criar(turmaId, request);
		URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
				.path("/api/aulas/{id}")
				.buildAndExpand(response.id())
				.toUri();
		return ResponseEntity.created(location).body(response);
	}

	@GetMapping("/api/turmas/{turmaId}/aulas")
	public List<AulaResponse> listarPorTurma(@PathVariable UUID turmaId) {
		return aulaService.listarPorTurma(turmaId);
	}

	@GetMapping("/api/aulas/{id}")
	public AulaResponse buscarPorId(@PathVariable UUID id) {
		return aulaService.buscarPorId(id);
	}

	@GetMapping(value = "/api/aulas/{id}/qrcode", produces = MediaType.IMAGE_PNG_VALUE)
	public byte[] obterQrCode(@PathVariable UUID id) {
		return aulaService.gerarImagemQrCode(id);
	}

}
