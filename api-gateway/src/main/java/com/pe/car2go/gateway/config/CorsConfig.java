package com.pe.car2go.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.cors.reactive.CorsWebFilter;

import java.util.List;

/**
 * Configuración CORS centralizada del API Gateway.
 * SEGURIDAD: se define en UN SOLO lugar (este filtro) para evitar que el header
 * Access-Control-Allow-Origin se duplique (lo que el navegador rechaza).
 * Se permite el frontend en localhost (desarrollo) y en Vercel (producción).
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration cors = new CorsConfiguration();
        // Origenes permitidos (frontend). allowedOriginPatterns permite comodines con credentials.
        cors.setAllowedOriginPatterns(List.of(
            "http://localhost:4200",
            "https://car2go-web.vercel.app",
            "https://*.vercel.app"
        ));
        cors.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        cors.setAllowedHeaders(List.of("*"));
        cors.setAllowCredentials(true);
        cors.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cors);
        return new CorsWebFilter(source);
    }
}
