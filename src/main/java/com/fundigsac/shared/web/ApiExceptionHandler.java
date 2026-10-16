package com.fundigsac.shared.web;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.fundigsac.shared.domain.ConflictException;
import com.fundigsac.shared.domain.DomainException;
import com.fundigsac.shared.domain.ForbiddenException;
import com.fundigsac.shared.domain.NotFoundException;
import com.fundigsac.shared.domain.RuleViolationException;
import com.fundigsac.shared.domain.StaleVersionException;
import com.fundigsac.shared.domain.UnauthorizedException;
import com.fundigsac.shared.domain.ValidationException;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.exc.UnrecognizedPropertyException;

/**
 * Traduce todas las excepciones a RFC 9457 (application/problem+json) con un código estable, la lista de campos
 * inválidos y el requestId. Nunca expone trazas, SQL, rutas internas ni si una cuenta existe.
 */
@RestControllerAdvice
class ApiExceptionHandler extends ResponseEntityExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

	record FieldProblem(String field, String code, String message) {
	}

	@ExceptionHandler(ValidationException.class)
	ResponseEntity<Object> validation(ValidationException ex, WebRequest request) {
		List<FieldProblem> errors = ex.violations()
			.stream()
			.map((v) -> new FieldProblem(v.field(), v.code(), v.message()))
			.toList();
		return problem(HttpStatus.BAD_REQUEST, ex.code(), ex.getMessage(), errors, request);
	}

	@ExceptionHandler(NotFoundException.class)
	ResponseEntity<Object> notFound(NotFoundException ex, WebRequest request) {
		return domain(HttpStatus.NOT_FOUND, ex, request);
	}

	@ExceptionHandler(ConflictException.class)
	ResponseEntity<Object> conflict(ConflictException ex, WebRequest request) {
		return domain(HttpStatus.CONFLICT, ex, request);
	}

	@ExceptionHandler(RuleViolationException.class)
	ResponseEntity<Object> rule(RuleViolationException ex, WebRequest request) {
		return domain(HttpStatus.UNPROCESSABLE_CONTENT, ex, request);
	}

	@ExceptionHandler(ForbiddenException.class)
	ResponseEntity<Object> forbidden(ForbiddenException ex, WebRequest request) {
		return domain(HttpStatus.FORBIDDEN, ex, request);
	}

	@ExceptionHandler(UnauthorizedException.class)
	ResponseEntity<Object> unauthorized(UnauthorizedException ex, WebRequest request) {
		return domain(HttpStatus.UNAUTHORIZED, ex, request);
	}

	@ExceptionHandler(StaleVersionException.class)
	ResponseEntity<Object> stale(StaleVersionException ex, WebRequest request) {
		return domain(HttpStatus.PRECONDITION_FAILED, ex, request);
	}

	@ExceptionHandler(ETags.PreconditionRequiredException.class)
	ResponseEntity<Object> preconditionRequired(WebRequest request) {
		return problem(HttpStatus.PRECONDITION_REQUIRED, "IF_MATCH_REQUIRED",
				"Envíe la cabecera If-Match con el ETag del registro.", List.of(), request);
	}

	@ExceptionHandler(TooManyRequestsException.class)
	ResponseEntity<Object> tooMany(TooManyRequestsException ex, WebRequest request) {
		ProblemDetail body = body(HttpStatus.TOO_MANY_REQUESTS, "RATE_LIMITED",
				"Demasiadas solicitudes. Espere un momento y vuelva a intentar.", List.of());
		return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
			.header(HttpHeaders.RETRY_AFTER, Long.toString(ex.retryAfterSeconds()))
			.body(body);
	}

	@ExceptionHandler(AccessDeniedException.class)
	ResponseEntity<Object> accessDenied(WebRequest request) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null || auth instanceof AnonymousAuthenticationToken) {
			return problem(HttpStatus.UNAUTHORIZED, "AUTHENTICATION_REQUIRED", "Inicie sesión para continuar.", List.of(),
					request);
		}
		return problem(HttpStatus.FORBIDDEN, "FORBIDDEN", "No tiene permiso para realizar esta acción.", List.of(), request);
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	ResponseEntity<Object> integrity(DataIntegrityViolationException ex, WebRequest request) {
		log.warn("Violación de integridad: {}", ex.getMostSpecificCause().getClass().getSimpleName());
		return problem(HttpStatus.CONFLICT, "CONFLICT", "El registro entra en conflicto con otro existente.", List.of(),
				request);
	}

	@ExceptionHandler(Exception.class)
	ResponseEntity<Object> unexpected(Exception ex, WebRequest request) {
		log.error("Error no controlado", ex);
		return problem(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR",
				"Ocurrió un error inesperado. Intente nuevamente en unos minutos.", List.of(), request);
	}

	@Override
	protected @Nullable ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
			HttpHeaders headers, HttpStatusCode status, WebRequest request) {
		List<FieldProblem> errors = new ArrayList<>();
		for (FieldError error : ex.getBindingResult().getFieldErrors()) {
			errors.add(new FieldProblem(error.getField(), codeOf(error.getCode()), message(error.getDefaultMessage())));
		}
		ex.getBindingResult()
			.getGlobalErrors()
			.forEach((error) -> errors.add(new FieldProblem(error.getObjectName(), codeOf(error.getCode()),
					message(error.getDefaultMessage()))));
		return problem(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Revise los campos indicados.", errors, request);
	}

	@Override
	protected @Nullable ResponseEntity<Object> handleHandlerMethodValidationException(HandlerMethodValidationException ex,
			HttpHeaders headers, HttpStatusCode status, WebRequest request) {
		List<FieldProblem> errors = ex.getParameterValidationResults()
			.stream()
			.flatMap((result) -> result.getResolvableErrors()
				.stream()
				.map((error) -> new FieldProblem(result.getMethodParameter().getParameterName(),
						codeOf(error.getCodes() == null || error.getCodes().length == 0 ? null
								: error.getCodes()[error.getCodes().length - 1]),
						message(error.getDefaultMessage()))))
			.toList();
		return problem(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Revise los campos indicados.", errors, request);
	}

	@Override
	protected @Nullable ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
			HttpHeaders headers, HttpStatusCode status, WebRequest request) {
		Throwable cause = ex.getCause();
		for (Throwable t = ex; t != null; t = t.getCause()) {
			if (t instanceof PayloadLimitFilter.PayloadTooLargeException) {
				return problem(HttpStatus.CONTENT_TOO_LARGE, "PAYLOAD_TOO_LARGE",
						"El contenido enviado supera el tamaño permitido.", List.of(), request);
			}
		}
		if (cause instanceof UnrecognizedPropertyException unknown) {
			return problem(HttpStatus.BAD_REQUEST, "UNKNOWN_FIELD", "El campo no está permitido: " + unknown.getPropertyName(),
					List.of(new FieldProblem(unknown.getPropertyName(), "UNKNOWN_FIELD", "Campo no permitido.")), request);
		}
		if (cause instanceof JacksonException jackson && !jackson.getPath().isEmpty()) {
			String field = jackson.getPath()
				.stream()
				.map((ref) -> ref.getPropertyName() != null ? ref.getPropertyName() : "[" + ref.getIndex() + "]")
				.collect(Collectors.joining(".")).replace(".[", "[");
			return problem(HttpStatus.BAD_REQUEST, "INVALID_VALUE", "Revise los campos indicados.",
					List.of(new FieldProblem(field, "INVALID_VALUE", "El valor no tiene el formato esperado.")), request);
		}
		return problem(HttpStatus.BAD_REQUEST, "MALFORMED_REQUEST", "El cuerpo de la solicitud no es un JSON válido.",
				List.of(), request);
	}

	@Override
	protected ResponseEntity<Object> createResponseEntity(@Nullable Object body, HttpHeaders headers,
			HttpStatusCode statusCode, WebRequest request) {
		if (body instanceof ProblemDetail detail) {
			if (detail.getProperties() == null || !detail.getProperties().containsKey("code")) {
				detail.setProperty("code", defaultCode(statusCode));
			}
			enrich(detail);
		}
		return super.createResponseEntity(body, headers, statusCode, request);
	}

	private ResponseEntity<Object> domain(HttpStatus status, DomainException ex, WebRequest request) {
		return problem(status, ex.code(), ex.getMessage(), List.of(), request);
	}

	private ResponseEntity<Object> problem(HttpStatus status, String code, String detail, List<FieldProblem> errors,
			WebRequest request) {
		return ResponseEntity.status(status).body(body(status, code, detail, errors));
	}

	private ProblemDetail body(HttpStatus status, String code, String detail, List<FieldProblem> errors) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
		problem.setType(URI.create("about:blank"));
		problem.setProperty("code", code);
		if (!errors.isEmpty()) {
			problem.setProperty("errors", errors);
		}
		enrich(problem);
		return problem;
	}

	private static void enrich(ProblemDetail problem) {
		String requestId = MDC.get(RequestIdFilter.MDC_KEY);
		if (requestId != null) {
			problem.setProperty("requestId", requestId);
		}
	}

	private static String defaultCode(HttpStatusCode status) {
		return switch (status.value()) {
			case 400 -> "BAD_REQUEST";
			case 404 -> "NOT_FOUND";
			case 405 -> "METHOD_NOT_ALLOWED";
			case 406 -> "NOT_ACCEPTABLE";
			case 413 -> "PAYLOAD_TOO_LARGE";
			case 415 -> "UNSUPPORTED_MEDIA_TYPE";
			default -> "HTTP_" + status.value();
		};
	}

	private static final Map<String, String> CONSTRAINT_CODES = Map.ofEntries(Map.entry("NotBlank", "REQUIRED"),
			Map.entry("NotNull", "REQUIRED"), Map.entry("NotEmpty", "REQUIRED"), Map.entry("Size", "INVALID_LENGTH"),
			Map.entry("Email", "INVALID_EMAIL"), Map.entry("Pattern", "INVALID_FORMAT"), Map.entry("Min", "OUT_OF_RANGE"),
			Map.entry("Max", "OUT_OF_RANGE"), Map.entry("Positive", "OUT_OF_RANGE"),
			Map.entry("PositiveOrZero", "OUT_OF_RANGE"), Map.entry("DecimalMin", "OUT_OF_RANGE"),
			Map.entry("DecimalMax", "OUT_OF_RANGE"), Map.entry("Digits", "OUT_OF_RANGE"),
			Map.entry("AssertTrue", "MUST_ACCEPT"), Map.entry("Past", "INVALID_DATE"),
			Map.entry("PastOrPresent", "INVALID_DATE"), Map.entry("FutureOrPresent", "INVALID_DATE"),
			Map.entry("Future", "INVALID_DATE"));

	private static String codeOf(@Nullable String constraint) {
		return constraint == null ? "INVALID_VALUE" : CONSTRAINT_CODES.getOrDefault(constraint, "INVALID_VALUE");
	}

	private static String message(@Nullable String message) {
		return message == null ? "Valor no válido." : message;
	}

}
