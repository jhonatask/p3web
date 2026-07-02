package br.com.familychurch.p3.turma.controller;

import br.com.familychurch.p3.turma.StatusTurma;
import br.com.familychurch.p3.turma.dto.TurmaDashboardResponse;
import br.com.familychurch.p3.turma.dto.TurmaPublicaResponse;
import br.com.familychurch.p3.turma.dto.TurmaRequest;
import br.com.familychurch.p3.turma.dto.TurmaResponse;
import br.com.familychurch.p3.turma.service.TurmaService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
public class TurmaController {

	private final TurmaService turmaService;

	public TurmaController(TurmaService turmaService) {
		this.turmaService = turmaService;
	}

	@PostMapping("/api/turmas")
	public ResponseEntity<TurmaResponse> criar(@Valid @RequestBody TurmaRequest request) {
		TurmaResponse response = turmaService.criar(request);
		URI location = ServletUriComponentsBuilder.fromCurrentRequest()
				.path("/{id}")
				.buildAndExpand(response.id())
				.toUri();
		return ResponseEntity.created(location).body(response);
	}

	@GetMapping("/api/turmas")
	public List<TurmaResponse> listar(
			@RequestParam(required = false) UUID cursoId,
			@RequestParam(required = false) StatusTurma status) {
		return turmaService.listar(cursoId, status);
	}

	@GetMapping("/api/turmas/{id}")
	public TurmaResponse buscarPorId(@PathVariable UUID id) {
		return turmaService.buscarPorId(id);
	}

	@PatchMapping("/api/turmas/{id}/encerrar")
	public TurmaResponse encerrar(@PathVariable UUID id) {
		return turmaService.encerrar(id);
	}

	@GetMapping("/api/publico/turmas/{token}")
	public TurmaPublicaResponse buscarPorTokenCadastro(@PathVariable String token) {
		return turmaService.buscarPorTokenCadastro(token);
	}

	@GetMapping("/api/turmas/{turmaId}/dashboard")
	public TurmaDashboardResponse obterDashboard(@PathVariable UUID turmaId) {
		return turmaService.obterDashboard(turmaId);
	}

}
