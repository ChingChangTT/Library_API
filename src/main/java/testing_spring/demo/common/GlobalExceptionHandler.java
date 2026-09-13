package testing_spring.demo.common;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.http.converter.HttpMessageNotReadableException;

import testing_spring.demo.book.BookNotFoundException;
import testing_spring.demo.book.BookUnavailableException;
import testing_spring.demo.book.BookValidationException;
import testing_spring.demo.skincare.SkincareProductNotFoundException;
import testing_spring.demo.skincare.SkincareProductValidationException;

@RestControllerAdvice
public class GlobalExceptionHandler {
	@ExceptionHandler(BookNotFoundException.class)
	public ResponseEntity<ApiError> handleBookNotFound(BookNotFoundException exception, HttpServletRequest request) {
		return buildError(HttpStatus.NOT_FOUND, exception.getMessage(), request);
	}

	@ExceptionHandler(SkincareProductNotFoundException.class)
	public ResponseEntity<ApiError> handleProductNotFound(SkincareProductNotFoundException exception, HttpServletRequest request) {
		return buildError(HttpStatus.NOT_FOUND, exception.getMessage(), request);
	}

	@ExceptionHandler(SearchResultNotFoundException.class)
	public ResponseEntity<ApiError> handleSearchNotFound(SearchResultNotFoundException exception, HttpServletRequest request) {
		return buildError(HttpStatus.NOT_FOUND, exception.getMessage(), request);
	}

	@ExceptionHandler({ BookValidationException.class, BookUnavailableException.class, SkincareProductValidationException.class })
	public ResponseEntity<ApiError> handleBadRequest(RuntimeException exception, HttpServletRequest request) {
		return buildError(HttpStatus.BAD_REQUEST, exception.getMessage(), request);
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<ApiError> handleDataIntegrityViolation(DataIntegrityViolationException exception, HttpServletRequest request) {
		String message = exception.getMostSpecificCause() != null ? exception.getMostSpecificCause().getMessage() : exception.getMessage();
		if (message != null && message.toLowerCase().contains("unique")) {
			message = "A record with the same unique value already exists.";
		}
		return buildError(HttpStatus.BAD_REQUEST, message, request);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException exception, HttpServletRequest request) {
		Map<String, String> fields = new LinkedHashMap<>();
		exception.getBindingResult().getFieldErrors()
				.forEach(error -> fields.putIfAbsent(error.getField(), error.getDefaultMessage()));
		return buildError(HttpStatus.BAD_REQUEST, "Request validation failed", request, fields);
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ApiError> handleUnreadableBody(HttpMessageNotReadableException exception, HttpServletRequest request) {
		return buildError(HttpStatus.BAD_REQUEST, "Request body is missing or malformed", request);
	}

	private ResponseEntity<ApiError> buildError(HttpStatus status, String message, HttpServletRequest request) {
		return buildError(status, message, request, Map.of());
	}

	private ResponseEntity<ApiError> buildError(HttpStatus status, String message, HttpServletRequest request,
			Map<String, String> fieldErrors) {
		ApiError error = new ApiError(
				Instant.now(),
				status.value(),
				status.getReasonPhrase(),
				message,
				request.getRequestURI(),
				fieldErrors
		);

		return ResponseEntity.status(status).body(error);
	}
}
