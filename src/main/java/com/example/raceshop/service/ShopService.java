package com.example.raceshop.service;

import com.example.raceshop.dto.ShopDtos.*;
import com.example.raceshop.model.*;
import com.example.raceshop.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.*;
import java.time.Instant;
import java.util.*;

@Service
public class ShopService {
	private final ProductRepository products;
	private final CartItemRepository cart;
	private final DiscountCodeRepository discounts;
	private final OrderRepository orders;
	private final UserRepository users;

	public ShopService(ProductRepository products, CartItemRepository cart, DiscountCodeRepository discounts,
			OrderRepository orders, UserRepository users) {
		this.products = products;
		this.cart = cart;
		this.discounts = discounts;
		this.orders = orders;
		this.users = users;
	}

	public List<Product> products() {
		return products.findAll();
	}

	private AppUser user(String email) {
		return users.findByEmailIgnoreCase(email).orElseThrow();
	}

	@Transactional
	public void add(String email, AddItemRequest r) {
		if (r.quantity() <= 0)
			throw new IllegalArgumentException("Quantity must be positive");
		AppUser u = user(email);
		Product p = products.findById(r.productId()).orElseThrow();
		CartItem i = cart.findByUserIdAndProductId(u.getId(), p.getId()).orElseGet(() -> {
			CartItem x = new CartItem();
			x.setUser(u);
			x.setProduct(p);
			return x;
		});
		i.setQuantity(i.getQuantity() + r.quantity());
		cart.save(i);
	}

	@Transactional
	public void setQuantity(String email, Long productId, QuantityRequest r) {
		if (r.quantity() < 0)
			throw new IllegalArgumentException("Quantity cannot be negative");
		AppUser u = user(email);
		CartItem i = cart.findByUserIdAndProductId(u.getId(), productId).orElseThrow();
		if (r.quantity() == 0)
			cart.delete(i);
		else {
			i.setQuantity(r.quantity());
			cart.save(i);
		}
	}

	public CartResponse cart(String email) {
		AppUser u = user(email);
		List<CartLine> lines = cart.findByUserId(u.getId()).stream()
				.map(i -> new CartLine(i.getProduct().getId(), i.getProduct().getName(), i.getQuantity(),
						i.getProduct().getPrice(),
						i.getProduct().getPrice().multiply(BigDecimal.valueOf(i.getQuantity()))))
				.toList();
		BigDecimal subtotal = lines.stream().map(CartLine::lineTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
		return pricedCart(lines, subtotal, u.getAppliedDiscountCodes());
	}

	@Transactional
	public CartResponse applyDiscount(String email, ApplyDiscountRequest r) {
		AppUser u = user(email);
		DiscountCode d = discounts.findByCodeIgnoreCase(r.code())
				.orElseThrow(() -> new IllegalArgumentException("Unknown discount code"));
		if (d.getUsedCount() >= d.getMaxUses())
			throw new IllegalStateException("Discount exhausted");
		if (hasCode(u.getAppliedDiscountCodes(), d.getCode()))
			throw new IllegalStateException("Code already applied to cart");
		// INTENTIONALLY VULNERABLE: checks and mutations are separated (TOCTOU window).
		sleepForRaceDemo();
		d.setUsedCount(d.getUsedCount() + 1);
		discounts.save(d);
		u.getAppliedDiscountCodes().add(d.getCode());
		users.save(u);
		return cart(email);
	}

	private static boolean hasCode(List<String> codes, String code) {
		return codes.stream().anyMatch(c -> c.equalsIgnoreCase(code));
	}

	private CartResponse pricedCart(List<CartLine> lines, BigDecimal subtotal, List<String> codes) {
		if (codes.isEmpty())
			return new CartResponse(lines, subtotal, List.of(), BigDecimal.ZERO, subtotal);
		List<String> applied = List.copyOf(codes);
		BigDecimal discount = BigDecimal.ZERO;
		for (String code : applied) {
			DiscountCode d = discounts.findByCodeIgnoreCase(code).orElseThrow();
			discount = discount.add(subtotal.multiply(d.getPercent()).divide(BigDecimal.valueOf(100), 2,
					RoundingMode.HALF_UP));
		}
		if (discount.compareTo(subtotal) > 0)
			discount = subtotal;
		return new CartResponse(lines, subtotal, applied, discount, subtotal.subtract(discount));
	}

	@Transactional
	public CreateOrderResponse order(String email) {
		AppUser u = user(email);
		List<CartItem> items = cart.findByUserId(u.getId());
		if (items.isEmpty())
			throw new IllegalStateException("Cart is empty");
		BigDecimal subtotal = items.stream()
				.map(i -> i.getProduct().getPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		List<String> discountCodes = List.copyOf(u.getAppliedDiscountCodes());
		CartResponse priced = pricedCart(List.of(), subtotal, discountCodes);
		BigDecimal total = priced.total();
		Order o = new Order();
		o.setUser(u);
		o.setSubtotal(subtotal);
		o.setTotal(total);
		o.setDiscountCodes(new ArrayList<>(discountCodes));
		o.setCreatedAt(Instant.now());
		orders.save(o);
		cart.deleteByUserId(u.getId());
		u.getAppliedDiscountCodes().clear();
		users.save(u);
		return new CreateOrderResponse(o.getId(), o.getTotal(), discountCodes);
	}

	private void sleepForRaceDemo() {
		try {
			Thread.sleep(150);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}
}
