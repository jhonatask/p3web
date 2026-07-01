package br.com.familychurch.p3.curso.controller;

import br.com.familychurch.p3.curso.dto.CursoRequest;
import br.com.familychurch.p3.curso.dto.CursoResponse;
import br.com.familychurch.p3.curso.exception.CursoJaExisteException;
import br.com.familychurch.p3.curso.exception.CursoNaoEncontradoException;
import br.com.familychurch.p3.curso.service.CursoService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/cursos")
public class CursoController {

	private final CursoService cursoService;

	public CursoController(CursoService cursoService) {
		this.cursoService = cursoService;
	}

	@PostMapping
	public ResponseEntity<CursoResponse> criar(@Valid @RequestBody CursoRequest request) {
		CursoResponse response = cursoService.criar(request);
		URI location = ServletUriComponentsBuilder.fromCurrentRequest()
				.path("/{id}")
				.buildAndExpand(response.id())
				.toUri();
		return ResponseEntity.created(location).body(response);
	}

	@GetMapping
	public List<CursoResponse> listar(@RequestParam(required = false) Boolean ativo) {
		return cursoService.listar(ativo);
	}

	@GetMapping("/{id}")
	public CursoResponse buscarPorId(@PathVariable UUID id) {
		return cursoService.buscarPorId(id);
	}

	@PutMapping("/{id}")
	public CursoResponse atualizar(@PathVariable UUID id, @Valid @RequestBody CursoRequest request) {
		return cursoService.atualizar(id, request);
	}

	@PatchMapping("/{id}/desativar")
	public CursoResponse desativar(@PathVariable UUID id) {
		return cursoService.desativar(id);
	}

	@ExceptionHandler(CursoJaExisteException.class)
	public ResponseEntity<String> tratarCursoJaExiste(CursoJaExisteException exception) {
		return ResponseEntity.status(409).body(exception.getMessage());
	}

	@ExceptionHandler(CursoNaoEncontradoException.class)
	public ResponseEntity<String> tratarCursoNaoEncontrado(CursoNaoEncontradoException exception) {
		return ResponseEntity.status(404).body(exception.getMessage());
	}

}
