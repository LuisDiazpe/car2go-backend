package com.pe.platform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class Car2goApplication {

    public static void main(String[] args) {
        SpringApplication.run(Car2goApplication.class, args);
    }
}
