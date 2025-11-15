package com.vsp.endpointinsightsapi.controller;

import com.vsp.endpointinsightsapi.util.CurrentUser;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	@RequestMapping("/user-info")
	public ResponseEntity<Map<String, Object>> getUserInfo(HttpServletRequest request) {

		var userContextOptional = CurrentUser.get();

		if (userContextOptional.isEmpty()) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}

		var userContext = userContextOptional.get();

		var response = Map.of(
				"username", userContext.getUsername(),
				"email", userContext.getEmail(),
				"roles", userContext.getRoles()
		);

		return ResponseEntity.ok(response);

	}

}
