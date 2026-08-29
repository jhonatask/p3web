package br.com.familychurch.p3.usuarioadmin.controller;

import br.com.familychurch.p3.usuarioadmin.Papel;
import br.com.familychurch.p3.usuarioadmin.dto.UsuarioAdminResponse;
import br.com.familychurch.p3.usuarioadmin.service.UsuarioAdminService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/usuarios-admin")
public class UsuarioAdminController {

	private final UsuarioAdminService usuarioAdminService;

	public UsuarioAdminController(UsuarioAdminService usuarioAdminService) {
		this.usuarioAdminService = usuarioAdminService;
	}

	@GetMapping
	public List<UsuarioAdminResponse> listar(@RequestParam(required = false) Papel papel) {
		return usuarioAdminService.listar(papel);
	}

}
