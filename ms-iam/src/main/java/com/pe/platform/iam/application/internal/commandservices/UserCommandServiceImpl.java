package com.pe.platform.iam.application.internal.commandservices;

import com.pe.platform.iam.application.internal.outboundservices.hashing.HashingService;
import com.pe.platform.iam.application.internal.outboundservices.tokens.TokenService;
import com.pe.platform.iam.domain.model.aggregates.User;
import com.pe.platform.iam.domain.model.commands.SeedRolesCommand;
import com.pe.platform.iam.domain.model.commands.SignInCommand;
import com.pe.platform.iam.domain.model.commands.SignUpCommand;
import com.pe.platform.iam.domain.model.entities.Role;
import com.pe.platform.iam.domain.model.valueobjects.Roles;
import com.pe.platform.iam.domain.services.UserCommandService;
import com.pe.platform.iam.infrastructure.persistence.jpa.repositories.RoleRepository;
import com.pe.platform.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Application service for User authentication use cases.
 * US-01: Registro con rol seleccionado (BUYER, SELLER, MECHANIC)
 * US-02: Login que retorna JWT real — corrige el bug de seguridad del frontend original
 */
@Service
@Transactional
public class UserCommandServiceImpl implements UserCommandService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final HashingService hashingService;
    private final TokenService tokenService;
    private final AuthenticationManager authenticationManager;

    public UserCommandServiceImpl(
            UserRepository userRepository,
            RoleRepository roleRepository,
            HashingService hashingService,
            TokenService tokenService,
            AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.hashingService = hashingService;
        this.tokenService = tokenService;
        this.authenticationManager = authenticationManager;
    }

    @Override
    public void handle(SeedRolesCommand command) {
        for (Roles roleName : Roles.values()) {
            if (!roleRepository.existsByName(roleName)) {
                roleRepository.save(new Role(roleName));
            }
        }
    }

    @Override
    public Optional<User> handle(SignUpCommand command) {
        if (userRepository.existsByUsername(command.username())) {
            throw new IllegalArgumentException("Username '" + command.username() + "' already exists");
        }
        if (userRepository.existsByEmail(command.email())) {
            throw new IllegalArgumentException("Email '" + command.email() + "' already exists");
        }

        List<Role> roles = new ArrayList<>();
        if (command.roles() == null || command.roles().isEmpty()) {
            roleRepository.findByName(Roles.ROLE_BUYER).ifPresent(roles::add);
        } else {
            command.roles().forEach(roleName -> {
                try {
                    Roles roleEnum = Roles.valueOf("ROLE_" + roleName.toUpperCase());
                    roleRepository.findByName(roleEnum).ifPresent(roles::add);
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException("Invalid role: " + roleName);
                }
            });
        }

        String passwordHash = hashingService.encode(command.password());
        User user = new User(command, passwordHash, roles);
        return Optional.of(userRepository.save(user));
    }

    @Override
    public Optional<ImmutablePair<User, String>> handle(SignInCommand command) {
        var authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(command.username(), command.password()));

        var token = tokenService.generateToken(authentication);
        return userRepository.findByUsername(command.username())
                .map(user -> ImmutablePair.of(user, token));
    }
}
