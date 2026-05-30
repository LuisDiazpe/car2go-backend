package com.pe.platform.iam.application.internal.commandservices;

import com.pe.platform.iam.application.internal.outboundservices.hashing.HashingService;
import com.pe.platform.iam.application.internal.outboundservices.tokens.TokenService;
import com.pe.platform.iam.domain.model.aggregates.User;
import com.pe.platform.iam.domain.model.commands.SignUpCommand;
import com.pe.platform.iam.domain.model.entities.Role;
import com.pe.platform.iam.domain.model.valueobjects.Roles;
import com.pe.platform.iam.infrastructure.persistence.jpa.repositories.RoleRepository;
import com.pe.platform.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserCommandServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private HashingService hashingService;
    @Mock private TokenService tokenService;
    @Mock private AuthenticationManager authenticationManager;

    @InjectMocks
    private UserCommandServiceImpl userCommandService;

    private Role buyerRole;
    private Role sellerRole;

    @BeforeEach
    void setUp() {
        buyerRole = new Role(Roles.ROLE_BUYER);
        sellerRole = new Role(Roles.ROLE_SELLER);
    }

    @Test
    void handle_SignUpCommand_WhenUsernameAlreadyExists_ShouldThrowException() {
        // Arrange
        var command = new SignUpCommand("vendedor1", "123456", List.of("SELLER"));
        when(userRepository.existsByUsername("vendedor1")).thenReturn(true);

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> userCommandService.handle(command));
        verify(userRepository, never()).save(any());
    }

    @Test
    void handle_SignUpCommand_WhenValidData_ShouldCreateUser() {
        // Arrange
        var command = new SignUpCommand("nuevouser", "123456", List.of("BUYER"));
        when(userRepository.existsByUsername("nuevouser")).thenReturn(false);
        when(roleRepository.findByName(Roles.ROLE_BUYER)).thenReturn(Optional.of(buyerRole));
        when(hashingService.encode("123456")).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        var result = userCommandService.handle(command);

        // Assert
        assertTrue(result.isPresent());
        verify(userRepository, times(1)).save(any(User.class));
        verify(hashingService, times(1)).encode("123456");
    }

    @Test
    void handle_SignUpCommand_WhenInvalidRole_ShouldThrowException() {
        // Arrange
        var command = new SignUpCommand("user1", "123456", List.of("INVALID_ROLE"));
        when(userRepository.existsByUsername("user1")).thenReturn(false);

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> userCommandService.handle(command));
    }
}