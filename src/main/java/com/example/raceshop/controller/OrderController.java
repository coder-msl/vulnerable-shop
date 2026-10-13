package com.example.raceshop.controller;

import com.example.raceshop.dto.ShopDtos.CreateOrderResponse;
import com.example.raceshop.service.ShopService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
	private final ShopService s;

	public OrderController(ShopService s) {
		this.s = s;
	}

	@PostMapping
	public CreateOrderResponse create(Authentication a) {
		return s.order(a.getName());
	}
}
