package com.example.raceshop.repository;

import com.example.raceshop.model.DiscountCode;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface DiscountCodeRepository extends JpaRepository<DiscountCode, Long> {
	Optional<DiscountCode> findByCodeIgnoreCase(String code);
}
