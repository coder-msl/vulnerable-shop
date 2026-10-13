package com.example.raceshop.controller;

import com.example.raceshop.dto.ShopDtos.*;
import com.example.raceshop.service.ShopService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
public class CartController {
	private final ShopService s;

	public CartController(ShopService s) {
		this.s = s;
	}

	@GetMapping
	public CartResponse get(Authentication a) {
		return s.cart(a.getName());
	}

	@PostMapping("/items")
	public CartResponse add(Authentication a, @RequestBody AddItemRequest r) {
		s.add(a.getName(), r);
		return s.cart(a.getName());
	}

	@PutMapping("/items/{productId}")
	public CartResponse set(Authentication a, @PathVariable Long productId, @RequestBody QuantityRequest r) {
		s.setQuantity(a.getName(), productId, r);
		return s.cart(a.getName());
	}

	@PostMapping("/discount")
	public CartResponse discount(Authentication a, @RequestBody ApplyDiscountRequest r) {
		return s.applyDiscount(a.getName(), r);
	}
}
