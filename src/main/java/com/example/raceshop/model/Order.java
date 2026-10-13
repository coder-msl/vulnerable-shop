package com.example.raceshop.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@ManyToOne(optional = false)
	private AppUser user;
	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal subtotal;
	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal total;
	@Column(nullable = false)
	private Instant createdAt;

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "order_discount_codes", joinColumns = @JoinColumn(name = "order_id"))
	@Column(name = "code", nullable = false)
	private List<String> discountCodes = new ArrayList<>();

	public Long getId() {
		return id;
	}

	public AppUser getUser() {
		return user;
	}

	public void setUser(AppUser v) {
		user = v;
	}

	public BigDecimal getSubtotal() {
		return subtotal;
	}

	public void setSubtotal(BigDecimal v) {
		subtotal = v;
	}

	public BigDecimal getTotal() {
		return total;
	}

	public void setTotal(BigDecimal v) {
		total = v;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Instant v) {
		createdAt = v;
	}

	public List<String> getDiscountCodes() {
		return discountCodes;
	}

	public void setDiscountCodes(List<String> v) {
		discountCodes = v;
	}
}
