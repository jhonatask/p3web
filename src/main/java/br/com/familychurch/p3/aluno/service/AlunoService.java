package br.com.familychurch.p3.aluno.service;

import br.com.familychurch.p3.aluno.dto.AlunoCadastroRequest;
import br.com.familychurch.p3.aluno.dto.AlunoCadastroResponse;
import br.com.familychurch.p3.aluno.dto.AlunoResponse;
import java.util.List;
import java.util.UUID;

public interface AlunoService {

	AlunoCadastroResponse cadastrar(String tokenCadastro, AlunoCadastroRequest request);

	List<AlunoResponse> listarPorTurma(UUID turmaId);

}
