package com.vsp.endpointinsightsapi.exception;

import org.springframework.http.HttpStatus;

import java.util.ArrayList;

public class CustomExceptionBuilder {

	private CustomException customException;
	private HttpStatus httpStatus;
	private ErrorResponse errorResponse;

	public CustomExceptionBuilder() {
		customException = new CustomException();
		errorResponse = new ErrorResponse();
	}

	public CustomExceptionBuilder(String... details) {
		this();
		withDetails(details);
	}

	public CustomExceptionBuilder(String detail) {
		this();
		withDetail(detail);
	}

	public CustomExceptionBuilder withError(String error) {
		errorResponse.setError(error);
		return this;
	}

	public CustomExceptionBuilder withDescription(String description) {
		errorResponse.setDescription(description);
		return this;
	}

	public CustomExceptionBuilder withDetail(String detail) {
		if (errorResponse.getDetails() == null) {
			errorResponse.setDetails(new ArrayList<>());
		}

		errorResponse.getDetails().add(detail);
		return this;
	}

	public CustomExceptionBuilder withDetails(String... details) {
		for (String detail : details) {
			withDetail(detail);
		}
		return this;
	}

	public CustomExceptionBuilder withStatus(HttpStatus httpStatus) {
		this.httpStatus = httpStatus;
		return this;
	}

	public CustomException build() {
		customException.setHttpStatus(httpStatus);
		customException.setErrorResponse(errorResponse);
		return customException;
	}

}
