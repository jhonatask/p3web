package br.com.familychurch.p3.common.exception;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@ExceptionHandler(NotFoundException.class)
	public ResponseEntity<ApiError> tratarNotFound(NotFoundException exception) {
		return construirResposta(HttpStatus.NOT_FOUND, exception.getMessage());
	}

	@ExceptionHandler(ConflictException.class)
	public ResponseEntity<ApiError> tratarConflict(ConflictException exception) {
		return construirResposta(HttpStatus.CONFLICT, exception.getMessage());
	}

	@ExceptionHandler(ValidationException.class)
	public ResponseEntity<ApiError> tratarValidation(ValidationException exception) {
		return construirResposta(HttpStatus.BAD_REQUEST, exception.getMessage());
	}

	@ExceptionHandler(UnauthorizedException.class)
	public ResponseEntity<ApiError> tratarUnauthorized(UnauthorizedException exception) {
		return construirResposta(HttpStatus.UNAUTHORIZED, exception.getMessage());
	}

	@ExceptionHandler(ForbiddenException.class)
	public ResponseEntity<ApiError> tratarForbidden(ForbiddenException exception) {
		return construirResposta(HttpStatus.FORBIDDEN, exception.getMessage());
	}

	@ExceptionHandler(NoResourceFoundException.class)
	public ResponseEntity<ApiError> tratarRecursoNaoEncontrado(NoResourceFoundException exception) {
		return construirResposta(HttpStatus.NOT_FOUND, "Recurso nao encontrado");
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiError> tratarArgumentoInvalido(MethodArgumentNotValidException exception) {
		List<ApiError.CampoInvalido> erros = exception.getBindingResult().getFieldErrors().stream()
				.map(this::paraCampoInvalido)
				.toList();
		ApiError corpo = ApiError.deValidacao(
				"Erro de validacao nos campos enviados", HttpStatus.BAD_REQUEST.value(), erros);
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(corpo);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiError> tratarErroGenerico(Exception exception) {
		LOGGER.error("Erro inesperado ao processar requisicao", exception);
		return construirResposta(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno inesperado. Tente novamente mais tarde.");
	}

	private ApiError.CampoInvalido paraCampoInvalido(FieldError erro) {
		return new ApiError.CampoInvalido(erro.getField(), erro.getDefaultMessage());
	}

	private ResponseEntity<ApiError> construirResposta(HttpStatus status, String mensagem) {
		return ResponseEntity.status(status).body(ApiError.de(mensagem, status.value()));
	}

}
