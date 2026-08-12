package br.com.familychurch.p3.participante.controller;

import br.com.familychurch.p3.participante.dto.ParticipanteCadastroRequest;
import br.com.familychurch.p3.participante.dto.ParticipanteCadastroResponse;
import br.com.familychurch.p3.participante.dto.ParticipanteResponse;
import br.com.familychurch.p3.participante.service.ParticipanteService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ParticipanteController {

	private final ParticipanteService participanteService;

	public ParticipanteController(ParticipanteService participanteService) {
		this.participanteService = participanteService;
	}

	@PostMapping("/api/publico/participantes")
	public ResponseEntity<ParticipanteCadastroResponse> cadastrar(@Valid @RequestBody ParticipanteCadastroRequest request) {
		ParticipanteCadastroResponse response = participanteService.cadastrar(request);
		return ResponseEntity.status(201).body(response);
	}

	@GetMapping("/api/participantes/{cpf}")
	public ParticipanteResponse buscarPorCpf(@PathVariable String cpf) {
		return participanteService.buscarPorCpf(cpf);
	}

}
