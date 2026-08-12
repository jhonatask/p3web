package br.com.familychurch.p3.common.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CpfValidatorTest {

	private CpfValidator cpfValidator;
	private ConstraintValidatorContext context;

	@BeforeEach
	void configurar() {
		cpfValidator = new CpfValidator();
		context = mock(ConstraintValidatorContext.class);
	}

	@Test
	void deveAceitarCpfValido() {
		assertThat(cpfValidator.isValid("529.982.247-25", context)).isTrue();
	}

	@Test
	void deveAceitarCpfValidoSomenteComDigitos() {
		assertThat(cpfValidator.isValid("52998224725", context)).isTrue();
	}

	@Test
	void deveRejeitarCpfComDigitoVerificadorErrado() {
		assertThat(cpfValidator.isValid("52998224700", context)).isFalse();
	}

	@Test
	void deveRejeitarCpfComTodosDigitosIguais() {
		assertThat(cpfValidator.isValid("11111111111", context)).isFalse();
	}

	@Test
	void deveRejeitarCpfComTamanhoErrado() {
		assertThat(cpfValidator.isValid("123456789", context)).isFalse();
	}

	@Test
	void deveRejeitarCpfNulo() {
		assertThat(cpfValidator.isValid(null, context)).isFalse();
	}

}
