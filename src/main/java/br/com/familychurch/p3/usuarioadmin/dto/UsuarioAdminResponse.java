package br.com.familychurch.p3.usuarioadmin.dto;

import br.com.familychurch.p3.usuarioadmin.UsuarioAdmin;
import java.util.UUID;

public record UsuarioAdminResponse(UUID id, String nome) {

	public static UsuarioAdminResponse from(UsuarioAdmin usuarioAdmin) {
		return new UsuarioAdminResponse(usuarioAdmin.getId(), usuarioAdmin.getNome());
	}

}
