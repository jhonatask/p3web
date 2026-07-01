package br.com.familychurch.p3.aluno.controller;

import br.com.familychurch.p3.aluno.dto.AlunoCadastroRequest;
import br.com.familychurch.p3.aluno.dto.AlunoCadastroResponse;
import br.com.familychurch.p3.aluno.dto.AlunoResponse;
import br.com.familychurch.p3.aluno.exception.AlunoJaCadastradoException;
import br.com.familychurch.p3.aluno.exception.ConsentimentoLgpdObrigatorioException;
import br.com.familychurch.p3.aluno.exception.TurmaEncerradaException;
import br.com.familychurch.p3.aluno.exception.TurmaNaoEncontradaException;
import br.com.familychurch.p3.aluno.service.AlunoService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
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

	@ExceptionHandler(TurmaNaoEncontradaException.class)
	public ResponseEntity<String> tratarTurmaNaoEncontrada(TurmaNaoEncontradaException exception) {
		return ResponseEntity.status(404).body(exception.getMessage());
	}

	@ExceptionHandler(TurmaEncerradaException.class)
	public ResponseEntity<String> tratarTurmaEncerrada(TurmaEncerradaException exception) {
		return ResponseEntity.status(409).body(exception.getMessage());
	}

	@ExceptionHandler(AlunoJaCadastradoException.class)
	public ResponseEntity<String> tratarAlunoJaCadastrado(AlunoJaCadastradoException exception) {
		return ResponseEntity.status(409).body(exception.getMessage());
	}

	@ExceptionHandler(ConsentimentoLgpdObrigatorioException.class)
	public ResponseEntity<String> tratarConsentimentoLgpdObrigatorio(ConsentimentoLgpdObrigatorioException exception) {
		return ResponseEntity.status(400).body(exception.getMessage());
	}

}
