package com.example.raceshop.repository;

import com.example.raceshop.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
	List<CartItem> findByUserId(Long userId);

	Optional<CartItem> findByUserIdAndProductId(Long userId, Long productId);

	void deleteByUserId(Long userId);
}
