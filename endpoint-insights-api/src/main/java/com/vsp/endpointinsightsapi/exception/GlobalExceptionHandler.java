package com.vsp.endpointinsightsapi.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class GlobalExceptionHandler {

	private static final Logger LOG = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@ExceptionHandler(CustomException.class)
	public ResponseEntity<ErrorResponse> handleCustomException(CustomException e) {
		return ResponseEntity.status(e.getHttpStatus()).body(e.getErrorResponse());
	}

	// Map validation exceptions to a specific, reusable format for easy error parsing
	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<ErrorResponse> handleValidationException(ConstraintViolationException e) {
		String[] details = e.getConstraintViolations().stream().map(ConstraintViolation::getMessage).toArray(String[]::new);
		CustomException ce = (new CustomExceptionBuilder(details))
				.withStatus(HttpStatus.BAD_REQUEST).build();
		return handleCustomException(ce);
	}

	@ExceptionHandler(ResponseStatusException.class)
	public ResponseEntity<ErrorResponse> handleResponseStatusException(ResponseStatusException e) {
		CustomException ce = (new CustomExceptionBuilder(e.getMessage()))
				.withStatus(HttpStatus.valueOf(e.getStatusCode().value())).build();
		return handleCustomException(ce);
	}

	// Temp exception handler
	@ExceptionHandler(Exception.class)
	public ResponseEntity<String> handleException(Exception e) {
		LOG.error("Error handled: {}, {} {}", e.getClass().getSimpleName(), e.getMessage(), e.getStackTrace());
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
	}
}
