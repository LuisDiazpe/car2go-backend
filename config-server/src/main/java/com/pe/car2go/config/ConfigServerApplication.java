package com.pe.car2go.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

/**
 * Spring Cloud Config Server.
 * Centraliza la configuracion de todos los microservicios en un solo lugar.
 * Modo "native": lee los archivos de configuracion desde el classpath
 * (carpeta resources/configurations) en vez de un repositorio Git externo.
 *
 * Responde al driver de CONFIGURACION EXTERNALIZADA de las slides del profe.
 */
@SpringBootApplication
@EnableConfigServer
public class ConfigServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(ConfigServerApplication.class, args);
    }
}
