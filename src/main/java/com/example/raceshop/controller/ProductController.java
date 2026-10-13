package com.example.raceshop.controller;

import com.example.raceshop.model.Product;
import com.example.raceshop.service.ShopService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {
	private final ShopService s;

	public ProductController(ShopService s) {
		this.s = s;
	}

	@GetMapping
	public List<Product> all() {
		return s.products();
	}
}
