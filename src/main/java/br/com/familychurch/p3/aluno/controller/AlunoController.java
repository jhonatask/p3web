package br.com.familychurch.p3.aluno.controller;

import br.com.familychurch.p3.aluno.dto.AlunoResponse;
import br.com.familychurch.p3.aluno.dto.MatriculaRequest;
import br.com.familychurch.p3.aluno.dto.MatriculaResponse;
import br.com.familychurch.p3.aluno.service.AlunoService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
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

	@GetMapping("/api/turmas/{turmaId}/alunos")
	public List<AlunoResponse> listarPorTurma(@PathVariable UUID turmaId) {
		return alunoService.listarPorTurma(turmaId);
	}

	@PostMapping("/api/publico/turmas/{turmaId}/matricula")
	public MatriculaResponse matricular(@PathVariable UUID turmaId, @Valid @RequestBody MatriculaRequest request) {
		return alunoService.matricularDireto(request.cpf(), turmaId);
	}

}
