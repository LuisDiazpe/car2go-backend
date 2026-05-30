package com.pe.platform.iam.infrastructure.tokens.jwt.services;

import com.pe.platform.iam.application.internal.outboundservices.tokens.TokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class TokenServiceImplTest {

    private TokenServiceImpl tokenService;

    @BeforeEach
    void setUp() {
        tokenService = new TokenServiceImpl();
        // Inyectar valores que normalmente vienen del application.properties
        ReflectionTestUtils.setField(tokenService, "secret",
                "TestSecretKeyParaJUnitDebeSerDe512BitsExactoParaHS512AlgorithmJWTSecurity1234567890ABCDEF12");
        ReflectionTestUtils.setField(tokenService, "expirationDays", 7);
        tokenService.init(); // llama al @PostConstruct manualmente
    }

    @Test
    void generateToken_WhenValidAuthentication_ShouldReturnNonEmptyToken() {
        // Arrange
        var userDetails = new org.springframework.security.core.userdetails.User(
                "vendedor1", "hashedPass",
                List.of(new SimpleGrantedAuthority("ROLE_SELLER")));
        var authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());

        // Act
        String token = tokenService.generateToken(authentication);

        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.startsWith("eyJ")); // todos los JWT empiezan con eyJ
    }

    @Test
    void getUsernameFromToken_WhenValidToken_ShouldReturnCorrectUsername() {
        // Arrange
        var userDetails = new org.springframework.security.core.userdetails.User(
                "comprador1", "hashedPass",
                List.of(new SimpleGrantedAuthority("ROLE_BUYER")));
        var authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
        String token = tokenService.generateToken(authentication);

        // Act
        String username = tokenService.getUsernameFromToken(token);

        // Assert
        assertEquals("comprador1", username);
    }

    @Test
    void validateToken_WhenValidToken_ShouldReturnTrue() {
        // Arrange
        var userDetails = new org.springframework.security.core.userdetails.User(
                "mecanico1", "hashedPass",
                List.of(new SimpleGrantedAuthority("ROLE_MECHANIC")));
        var authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
        String token = tokenService.generateToken(authentication);

        // Act & Assert
        assertTrue(tokenService.validateToken(token));
    }

    @Test
    void validateToken_WhenInvalidToken_ShouldReturnFalse() {
        // Arrange
        String tokenInvalido = "eyJhbGciOiJIUzUxMiJ9" +
                ".eyJzdWIiOiJ0ZXN0IiwiaWF0IjoxNjAwMDAwMDAwLCJleHAiOjE2MDAwMDAwMDF9" +
                ".AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";

        // Act & Assert
        assertFalse(tokenService.validateToken(tokenInvalido));
    }

    @Test
    void validateToken_WhenEmptyToken_ShouldReturnFalse() {
        // Act & Assert
        assertFalse(tokenService.validateToken(""));
    }
}