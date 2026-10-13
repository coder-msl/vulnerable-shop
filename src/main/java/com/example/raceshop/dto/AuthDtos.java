package com.example.raceshop.dto;

public final class AuthDtos {
	public record RegisterRequest(String email, String firstName, String lastName, String password) {
	}

	public record LoginRequest(String email, String password) {
	}

	public record LoginResponse(String token) {
	}
}
