package com.pe.platform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/** Microservicio Vehicle Management - catalogo, publicacion, busqueda de autos. BD: db_vehicle */
@SpringBootApplication
@EnableJpaAuditing
@EnableDiscoveryClient
public class MsVehicleApplication {
    public static void main(String[] args) {
        SpringApplication.run(MsVehicleApplication.class, args);
    }
}
