package com.sesac.carematching;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.util.TimeZone;

@EnableJpaAuditing
@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.sesac.carematching")
public class CarematchingApplication {

	public static void main(String[] args) {
		SpringApplication.run(CarematchingApplication.class, args);
	}

    @PostConstruct
    public void init() {
        // timezone 설정
        TimeZone.setDefault(TimeZone.getTimeZone("GMT+9:00"));
    }
}
