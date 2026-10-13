package com.example.raceshop.config;

import com.example.raceshop.security.JwtAuthFilter;
import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {
	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	SecurityFilterChain security(HttpSecurity http, JwtAuthFilter filter) throws Exception {
		return http.csrf(csrf -> csrf.disable())
				.sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(a -> a
						.requestMatchers("/", "/index.html", "/favicon.ico", "/api/auth/**", "/api/products/**",
								"/h2-console/**")
						.permitAll().anyRequest().authenticated())
				.headers(h -> h.frameOptions(f -> f.sameOrigin()))
				.addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class).build();
	}
}
