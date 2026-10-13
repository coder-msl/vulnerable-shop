package com.example.raceshop.dto;

import java.math.BigDecimal;
import java.util.List;

public final class ShopDtos {
	public record AddItemRequest(Long productId, int quantity) {
	}

	public record QuantityRequest(int quantity) {
	}

	public record ApplyDiscountRequest(String code) {
	}

	public record CartLine(Long productId, String name, int quantity, BigDecimal unitPrice, BigDecimal lineTotal) {
	}

	public record CartResponse(List<CartLine> items, BigDecimal subtotal, List<String> discountCodes,
			BigDecimal discount, BigDecimal total) {
	}

	public record CreateOrderResponse(Long orderId, BigDecimal total, List<String> discountCodes) {
	}
}
