package com.example.raceshop.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "app_users")
public class AppUser {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(nullable = false, unique = true)
	private String email;
	@Column(nullable = false)
	private String firstName;
	@Column(nullable = false)
	private String lastName;
	@Column(nullable = false)
	private String passwordHash;
	@Column(nullable = false)
	private int failedAttempts;
	@Column(nullable = false)
	private boolean locked;

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "user_applied_discount_codes", joinColumns = @JoinColumn(name = "user_id"))
	@Column(name = "code", nullable = false)
	private List<String> appliedDiscountCodes = new ArrayList<>();

	public Long getId() {
		return id;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getPasswordHash() {
		return passwordHash;
	}

	public void setPasswordHash(String passwordHash) {
		this.passwordHash = passwordHash;
	}

	public int getFailedAttempts() {
		return failedAttempts;
	}

	public void setFailedAttempts(int failedAttempts) {
		this.failedAttempts = failedAttempts;
	}

	public boolean isLocked() {
		return locked;
	}

	public List<String> getAppliedDiscountCodes() {
		return appliedDiscountCodes;
	}

	public void setAppliedDiscountCodes(List<String> appliedDiscountCodes) {
		this.appliedDiscountCodes = appliedDiscountCodes;
	}

	public void setLocked(boolean locked) {
		this.locked = locked;
	}
}
