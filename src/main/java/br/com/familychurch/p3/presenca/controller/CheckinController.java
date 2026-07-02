package br.com.familychurch.p3.presenca.controller;

import br.com.familychurch.p3.presenca.dto.CheckinRequest;
import br.com.familychurch.p3.presenca.dto.PresencaResponse;
import br.com.familychurch.p3.presenca.service.PresencaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CheckinController {

	private final PresencaService presencaService;

	public CheckinController(PresencaService presencaService) {
		this.presencaService = presencaService;
	}

	@PostMapping("/api/publico/checkin")
	public ResponseEntity<PresencaResponse> checkin(@Valid @RequestBody CheckinRequest request) {
		return ResponseEntity.status(201).body(presencaService.checkin(request));
	}

}
