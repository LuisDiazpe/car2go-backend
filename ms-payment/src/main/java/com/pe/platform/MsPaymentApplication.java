package com.pe.platform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/** Microservicio Payment - transacciones de compra/venta. BD: db_payment */
@SpringBootApplication
@EnableJpaAuditing
@EnableDiscoveryClient
public class MsPaymentApplication {
    public static void main(String[] args) {
        SpringApplication.run(MsPaymentApplication.class, args);
    }
}
