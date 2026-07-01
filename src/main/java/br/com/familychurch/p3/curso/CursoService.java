package br.com.familychurch.p3.curso;

import java.util.List;
import java.util.UUID;

public interface CursoService {

	CursoResponse criar(CursoRequest request);

	List<CursoResponse> listar(Boolean ativo);

	CursoResponse buscarPorId(UUID id);

	CursoResponse atualizar(UUID id, CursoRequest request);

	CursoResponse desativar(UUID id);

}
