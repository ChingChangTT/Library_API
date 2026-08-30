package testing_spring.demo.common;

import java.time.Instant;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import testing_spring.demo.book.BookNotFoundException;
import testing_spring.demo.book.BookUnavailableException;
import testing_spring.demo.book.BookValidationException;

@RestControllerAdvice
public class GlobalExceptionHandler {
	@ExceptionHandler(BookNotFoundException.class)
	public ResponseEntity<ApiError> handleBookNotFound(BookNotFoundException exception, HttpServletRequest request) {
		return buildError(HttpStatus.NOT_FOUND, exception.getMessage(), request);
	}

	@ExceptionHandler({ BookValidationException.class, BookUnavailableException.class })
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

	private ResponseEntity<ApiError> buildError(HttpStatus status, String message, HttpServletRequest request) {
		ApiError error = new ApiError(
				Instant.now(),
				status.value(),
				status.getReasonPhrase(),
				message,
				request.getRequestURI()
		);

		return ResponseEntity.status(status).body(error);
	}
}
