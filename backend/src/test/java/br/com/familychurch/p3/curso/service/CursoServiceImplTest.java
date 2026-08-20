package br.com.familychurch.p3.curso.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.familychurch.p3.curso.Curso;
import br.com.familychurch.p3.curso.dto.CursoRequest;
import br.com.familychurch.p3.curso.dto.CursoResponse;
import br.com.familychurch.p3.curso.exception.CursoJaExisteException;
import br.com.familychurch.p3.curso.exception.CursoNaoEncontradoException;
import br.com.familychurch.p3.curso.repository.CursoRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CursoServiceImplTest {

	@Mock
	private CursoRepository cursoRepository;

	@InjectMocks
	private CursoServiceImpl cursoService;

	@Test
	void deveLancarCursoJaExisteExceptionAoCriarCursoComNomeDuplicado() {
		Curso cursoExistente = new Curso();
		cursoExistente.setNome("Escola de Hombridade");
		cursoExistente.setAtivo(true);
		when(cursoRepository.findByNomeIgnoreCase("Escola de Hombridade")).thenReturn(Optional.of(cursoExistente));

		CursoRequest request = new CursoRequest("Escola de Hombridade", "descricao");

		assertThatThrownBy(() -> cursoService.criar(request))
				.isInstanceOf(CursoJaExisteException.class);

		verify(cursoRepository, never()).save(any());
	}

	@Test
	void devePermitirCriarCursoQuandoNomeExistenteEstaInativo() {
		Curso cursoInativo = new Curso();
		cursoInativo.setNome("Escola de Hombridade");
		cursoInativo.setAtivo(false);
		when(cursoRepository.findByNomeIgnoreCase("Escola de Hombridade")).thenReturn(Optional.of(cursoInativo));
		when(cursoRepository.save(any(Curso.class))).thenAnswer(invocation -> invocation.getArgument(0));

		CursoRequest request = new CursoRequest("Escola de Hombridade", "descricao");

		CursoResponse response = cursoService.criar(request);

		assertThat(response.nome()).isEqualTo("Escola de Hombridade");
		assertThat(response.ativo()).isTrue();
	}

	@Test
	void deveLancarCursoNaoEncontradoExceptionAoBuscarIdInexistente() {
		UUID idInexistente = UUID.randomUUID();
		when(cursoRepository.findById(idInexistente)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> cursoService.buscarPorId(idInexistente))
				.isInstanceOf(CursoNaoEncontradoException.class);
	}

}
