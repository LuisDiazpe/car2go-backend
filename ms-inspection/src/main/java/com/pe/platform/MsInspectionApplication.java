package com.pe.platform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/** Microservicio Inspection - certificacion del mecanico
 * BD: db_inspection
 *  Usa Feign para comunicarse con ms-vehicle */
@SpringBootApplication
@EnableJpaAuditing
@EnableDiscoveryClient
@EnableFeignClients
public class MsInspectionApplication {
    public static void main(String[] args) {
        SpringApplication.run(MsInspectionApplication.class, args);
    }
}
