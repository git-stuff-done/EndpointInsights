package com.vsp.endpointinsightsapi.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CustomException extends RuntimeException {
	private HttpStatus httpStatus;
	private ErrorResponse errorResponse;
}
