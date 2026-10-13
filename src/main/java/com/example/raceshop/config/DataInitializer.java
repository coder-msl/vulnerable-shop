package com.example.raceshop.config;

import com.example.raceshop.model.*;
import com.example.raceshop.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.math.BigDecimal;

@Configuration
public class DataInitializer {
	@Bean
	CommandLineRunner seed(ProductRepository p, DiscountCodeRepository d, UserRepository u, PasswordEncoder e) {
		return args -> {
			Product a = new Product();
			a.setName("Mechanical Keyboard");
			a.setPrice(new BigDecimal("399.00"));
			a.setDescription("Demo mechanical keyboard");
			p.save(a);
			Product b = new Product();
			b.setName("USB-C Dock");
			b.setPrice(new BigDecimal("249.00"));
			b.setDescription("Demo USB-C docking station");
			p.save(b);
			Product c = new Product();
			c.setName("Security Book");
			c.setPrice(new BigDecimal("129.00"));
			c.setDescription("AppSec training book");
			p.save(c);
			DiscountCode x = new DiscountCode();
			x.setCode("RACE10");
			x.setPercent(new BigDecimal("10.00"));
			x.setMaxUses(1);
			d.save(x);
			DiscountCode y = new DiscountCode();
			y.setCode("WELCOME5");
			y.setPercent(new BigDecimal("5.00"));
			y.setMaxUses(100);
			d.save(y);
			AppUser u1 = new AppUser();
			u1.setEmail("demo@example.com");
			u1.setFirstName("Demo");
			u1.setLastName("User");
			u1.setPasswordHash(e.encode("Password123!"));
			u.save(u1);
		};
	}
}
