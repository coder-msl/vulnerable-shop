package com.example.raceshop.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "discount_codes")
public class DiscountCode {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(nullable = false, unique = true)
	private String code;
	@Column(nullable = false, precision = 5, scale = 2)
	private BigDecimal percent;
	@Column(nullable = false)
	private int maxUses;
	@Column(nullable = false)
	private int usedCount;

	public Long getId() {
		return id;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String v) {
		code = v;
	}

	public BigDecimal getPercent() {
		return percent;
	}

	public void setPercent(BigDecimal v) {
		percent = v;
	}

	public int getMaxUses() {
		return maxUses;
	}

	public void setMaxUses(int v) {
		maxUses = v;
	}

	public int getUsedCount() {
		return usedCount;
	}

	public void setUsedCount(int v) {
		usedCount = v;
	}
}
