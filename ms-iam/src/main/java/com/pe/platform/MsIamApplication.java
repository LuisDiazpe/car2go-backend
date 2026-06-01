package com.pe.platform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Microservicio IAM - Identidad, Autenticacion y Autorizacion
 * Responsabilidad: registro, login, JWT, roles
 * BD propia: db_iam (independiente de los demas microservicios)
 */
@SpringBootApplication
@EnableJpaAuditing
@EnableDiscoveryClient
public class MsIamApplication {
    public static void main(String[] args) {
        SpringApplication.run(MsIamApplication.class, args);
    }
}
