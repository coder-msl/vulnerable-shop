package com.example.raceshop.service;

import com.example.raceshop.dto.AuthDtos.*;
import com.example.raceshop.model.AppUser;
import com.example.raceshop.repository.UserRepository;
import com.example.raceshop.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
	private final UserRepository users;
	private final PasswordEncoder encoder;
	private final JwtService jwt;

	public AuthService(UserRepository users, PasswordEncoder encoder, JwtService jwt) {
		this.users = users;
		this.encoder = encoder;
		this.jwt = jwt;
	}

	@Transactional
	public void register(RegisterRequest r) {
		if (users.findByEmailIgnoreCase(r.email()).isPresent())
			throw new IllegalArgumentException("Email already registered");
		AppUser u = new AppUser();
		u.setEmail(r.email());
		u.setFirstName(r.firstName());
		u.setLastName(r.lastName());
		u.setPasswordHash(encoder.encode(r.password()));
		users.save(u);
	}

	@Transactional
	public LoginResponse login(LoginRequest r) {
		AppUser u = users.findByEmailIgnoreCase(r.email())
				.orElseThrow(() -> new IllegalArgumentException("No such user - I am sorry"));
		
		if (u.isLocked()) {
			throw new IllegalStateException("Account locked");
		}
		if (!encoder.matches(r.password(), u.getPasswordHash())) {
			int next = u.getFailedAttempts() + 1;
			sleepForRaceDemo();
			u.setFailedAttempts(next);
			if (next >= 3) {
				u.setLocked(true);
			}
			users.save(u);
			return new LoginResponse("Wrong password, attempts left: " + (3 - next));
		}
		u.setFailedAttempts(0);
		users.save(u);
		return new LoginResponse(jwt.generate(u.getEmail()));
	}

	private void sleepForRaceDemo() {
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}
}
