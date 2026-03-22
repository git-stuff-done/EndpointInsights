package com.vsp.endpointinsightsapi.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class JobSetupException extends RuntimeException {

	@Getter
	private final String message;

	@Getter
	private final Exception cause;

}
