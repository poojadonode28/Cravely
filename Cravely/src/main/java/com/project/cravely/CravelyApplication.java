package com.project.cravely;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class CravelyApplication {

	public static void main(String[] args) {
		SpringApplication.run(CravelyApplication.class, args);
	}

	@Bean
    CommandLineRunner printDemoPasswordHashes(
			PasswordEncoder passwordEncoder
	) {
		return args -> {
			System.out.println("ADMIN_HASH=" +
					passwordEncoder.encode("Admin@123"));

			System.out.println("OWNER_HASH=" +
					passwordEncoder.encode("Owner@123"));

			System.out.println("CUSTOMER_HASH=" +
					passwordEncoder.encode("Customer@123"));

			System.out.println("DELIVERY_HASH=" +
					passwordEncoder.encode("Delivery@123"));
		};
	}

}
