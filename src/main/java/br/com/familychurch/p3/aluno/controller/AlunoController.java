package br.com.familychurch.p3.aluno.controller;

import br.com.familychurch.p3.aluno.dto.AlunoCadastroRequest;
import br.com.familychurch.p3.aluno.dto.AlunoCadastroResponse;
import br.com.familychurch.p3.aluno.dto.AlunoResponse;
import br.com.familychurch.p3.aluno.service.AlunoService;
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
public class AlunoController {

	private final AlunoService alunoService;

	public AlunoController(AlunoService alunoService) {
		this.alunoService = alunoService;
	}

	@PostMapping("/api/publico/turmas/{token}/alunos")
	public ResponseEntity<AlunoCadastroResponse> cadastrar(
			@PathVariable String token,
			@Valid @RequestBody AlunoCadastroRequest request) {
		AlunoCadastroResponse response = alunoService.cadastrar(token, request);
		return ResponseEntity.status(201).body(response);
	}

	@GetMapping("/api/turmas/{turmaId}/alunos")
	public List<AlunoResponse> listarPorTurma(@PathVariable UUID turmaId) {
		return alunoService.listarPorTurma(turmaId);
	}

}
