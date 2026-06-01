package com.pe.car2go.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * API Gateway
 * Enruta las peticiones a cada microservicio descubriendolos via Eureka
 * El cliente  solo conoce esta URL, no las de los 5 servicios
 */
@SpringBootApplication
public class ApiGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
