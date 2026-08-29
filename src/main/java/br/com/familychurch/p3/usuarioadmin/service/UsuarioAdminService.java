package br.com.familychurch.p3.usuarioadmin.service;

import br.com.familychurch.p3.usuarioadmin.Papel;
import br.com.familychurch.p3.usuarioadmin.dto.UsuarioAdminResponse;
import java.util.List;

public interface UsuarioAdminService {

	List<UsuarioAdminResponse> listar(Papel papel);

}
