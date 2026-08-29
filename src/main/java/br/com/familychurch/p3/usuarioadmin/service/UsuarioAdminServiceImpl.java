package br.com.familychurch.p3.usuarioadmin.service;

import br.com.familychurch.p3.usuarioadmin.Papel;
import br.com.familychurch.p3.usuarioadmin.UsuarioAdmin;
import br.com.familychurch.p3.usuarioadmin.dto.UsuarioAdminResponse;
import br.com.familychurch.p3.usuarioadmin.repository.UsuarioAdminRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class UsuarioAdminServiceImpl implements UsuarioAdminService {

	private final UsuarioAdminRepository usuarioAdminRepository;

	public UsuarioAdminServiceImpl(UsuarioAdminRepository usuarioAdminRepository) {
		this.usuarioAdminRepository = usuarioAdminRepository;
	}

	@Override
	public List<UsuarioAdminResponse> listar(Papel papel) {
		List<UsuarioAdmin> usuarios = papel == null
				? usuarioAdminRepository.findAll()
				: usuarioAdminRepository.findByPapel(papel);
		return usuarios.stream().map(UsuarioAdminResponse::from).toList();
	}

}
