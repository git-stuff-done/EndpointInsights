package com.vsp.endpointinsightsapi.exception;

import jakarta.validation.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

	private static final Logger LOG = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@ExceptionHandler(CustomException.class)
	public ResponseEntity<ErrorResponse> handleCustomException(CustomException e) {
		return ResponseEntity.status(e.getHttpStatus()).body(e.getErrorResponse());
	}

	// Map validation exceptions to a specific, reusable format for easy error parsing
	@ExceptionHandler(ValidationException.class)
	public ResponseEntity<ErrorResponse> handleValidationException(ValidationException e) {
		CustomException ce = (new CustomExceptionBuilder(e.getMessage())).withStatus(HttpStatus.BAD_REQUEST).build();
		return handleCustomException(ce);
	}

	// Temp exception handler
	@ExceptionHandler(Exception.class)
	public ResponseEntity<String> handleException(Exception e) {
		LOG.error("Error handled: {}, {} {}", e.getClass().getSimpleName(), e.getMessage(), e.getStackTrace());
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
	}
}
