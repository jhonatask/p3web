package br.com.familychurch.p3.aula.controller;

import br.com.familychurch.p3.aula.dto.TipoAulaResponse;
import br.com.familychurch.p3.aula.repository.TipoAulaRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TipoAulaController {

	private final TipoAulaRepository tipoAulaRepository;

	public TipoAulaController(TipoAulaRepository tipoAulaRepository) {
		this.tipoAulaRepository = tipoAulaRepository;
	}

	@GetMapping("/api/cursos/{cursoId}/tipos-aula")
	public List<TipoAulaResponse> listarPorCurso(@PathVariable UUID cursoId) {
		return tipoAulaRepository.findByCursoIdOrderByOrdemAsc(cursoId).stream().map(TipoAulaResponse::from).toList();
	}

}
