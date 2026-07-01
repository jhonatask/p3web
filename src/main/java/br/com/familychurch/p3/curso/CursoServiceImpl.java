package br.com.familychurch.p3.curso;

import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
class CursoServiceImpl implements CursoService {

	private final CursoRepository cursoRepository;

	CursoServiceImpl(CursoRepository cursoRepository) {
		this.cursoRepository = cursoRepository;
	}

	@Override
	public CursoResponse criar(CursoRequest request) {
		validarNomeDisponivel(request.nome());

		Curso curso = new Curso();
		curso.setNome(request.nome());
		curso.setDescricao(request.descricao());
		curso.setAtivo(true);

		return CursoResponse.from(cursoRepository.save(curso));
	}

	@Override
	@Transactional(readOnly = true)
	public List<CursoResponse> listar(Boolean ativo) {
		List<Curso> cursos = ativo == null ? cursoRepository.findAll() : cursoRepository.findByAtivo(ativo);
		return cursos.stream().map(CursoResponse::from).toList();
	}

	@Override
	@Transactional(readOnly = true)
	public CursoResponse buscarPorId(UUID id) {
		return CursoResponse.from(buscarEntidadePorId(id));
	}

	@Override
	public CursoResponse atualizar(UUID id, CursoRequest request) {
		Curso curso = buscarEntidadePorId(id);

		if (!curso.getNome().equalsIgnoreCase(request.nome())) {
			validarNomeDisponivel(request.nome());
		}

		curso.setNome(request.nome());
		curso.setDescricao(request.descricao());

		return CursoResponse.from(curso);
	}

	@Override
	public CursoResponse desativar(UUID id) {
		Curso curso = buscarEntidadePorId(id);
		curso.setAtivo(false);
		return CursoResponse.from(curso);
	}

	private Curso buscarEntidadePorId(UUID id) {
		return cursoRepository.findById(id).orElseThrow(() -> new CursoNaoEncontradoException(id));
	}

	private void validarNomeDisponivel(String nome) {
		cursoRepository.findByNomeIgnoreCase(nome)
				.filter(Curso::isAtivo)
				.ifPresent(curso -> {
					throw new CursoJaExisteException(nome);
				});
	}

}
