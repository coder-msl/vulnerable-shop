package com.example.raceshop.controller;

import com.example.raceshop.dto.AuthDtos.*;
import com.example.raceshop.service.AuthService;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
	private final AuthService s;

	public AuthController(AuthService s) {
		this.s = s;
	}

	@PostMapping("/register")
	public ResponseEntity<?> register(@RequestBody RegisterRequest r) {
		try {
			s.register(r);
			return ResponseEntity.status(201).build();
		} catch (Exception e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}
	}

	@PostMapping("/login")
	public ResponseEntity<?> login(@RequestBody LoginRequest r) {
		try {
			return ResponseEntity.ok(s.login(r));
		} catch (IllegalStateException e) {
			return ResponseEntity.status(423).body(e.getMessage());
		} catch (Exception e) {
			return ResponseEntity.status(401).body(e.getMessage());
		}
	}
}
