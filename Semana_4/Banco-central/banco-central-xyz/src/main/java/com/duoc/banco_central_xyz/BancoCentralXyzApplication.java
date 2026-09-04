package com.duoc.banco_central_xyz;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
public class BancoCentralXyzApplication {

	public static void main(String[] args) {
		SpringApplication.run(BancoCentralXyzApplication.class, args);
	}

}
