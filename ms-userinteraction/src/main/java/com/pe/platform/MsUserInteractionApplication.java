package com.pe.platform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/** Microservicio User Interaction - favoritos, comentarios y reseñas
 *  BD: db_userinteraction
 *  Usa Feign para consultar transacciones a ms-payment (validación de reseñas) */
@SpringBootApplication
@EnableJpaAuditing
@EnableDiscoveryClient
@EnableFeignClients
public class MsUserInteractionApplication {
    public static void main(String[] args) {
        SpringApplication.run(MsUserInteractionApplication.class, args);
    }
}
