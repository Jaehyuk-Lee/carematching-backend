package com.sesac.carematching;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;

@EnableJpaAuditing
@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.sesac.carematching")
public class CarematchingApplication {

	public static void main(String[] args) {
		SpringApplication.run(CarematchingApplication.class, args);
	}
}
