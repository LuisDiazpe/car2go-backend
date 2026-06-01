package com.pe.platform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/** Microservicio User Interaction - favoritos del comprador BD: db_userinteraction */
@SpringBootApplication
@EnableJpaAuditing
@EnableDiscoveryClient
public class MsUserInteractionApplication {
    public static void main(String[] args) {
        SpringApplication.run(MsUserInteractionApplication.class, args);
    }
}
