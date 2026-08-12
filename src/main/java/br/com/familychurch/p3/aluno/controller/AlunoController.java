package br.com.familychurch.p3.aluno.controller;

import br.com.familychurch.p3.aluno.dto.AlunoResponse;
import br.com.familychurch.p3.aluno.service.AlunoService;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

}
