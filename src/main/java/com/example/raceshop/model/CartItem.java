package com.example.raceshop.model;

import jakarta.persistence.*;

@Entity
@Table(name = "cart_items")
public class CartItem {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@ManyToOne(optional = false)
	private AppUser user;
	@ManyToOne(optional = false)
	private Product product;
	@Column(nullable = false)
	private int quantity;

	public Long getId() {
		return id;
	}

	public AppUser getUser() {
		return user;
	}

	public void setUser(AppUser v) {
		user = v;
	}

	public Product getProduct() {
		return product;
	}

	public void setProduct(Product v) {
		product = v;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int v) {
		quantity = v;
	}
}
