package com.pe.platform.iam.interfaces.rest;

import com.pe.platform.iam.domain.model.commands.SignInCommand;
import com.pe.platform.iam.domain.model.commands.SignUpCommand;
import com.pe.platform.iam.domain.services.UserCommandService;
import com.pe.platform.iam.interfaces.rest.resources.AuthenticatedUserResource;
import com.pe.platform.iam.interfaces.rest.resources.SignInResource;
import com.pe.platform.iam.interfaces.rest.resources.SignUpResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication REST controller.
 * Expone los endpoints de registro (US-01) y login con JWT real (US-02).
 * Corrige el bug crítico del frontend original que enviaba credenciales en la URL.
 */
@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Registro e inicio de sesión de usuarios")
public class AuthenticationController {

    private final UserCommandService userCommandService;

    public AuthenticationController(UserCommandService userCommandService) {
        this.userCommandService = userCommandService;
    }

    /**
     * US-01: Registro de usuario con rol (BUYER, SELLER, MECHANIC)
     */
    @PostMapping("/sign-up")
    @Operation(summary = "Registrar nuevo usuario", description = "Roles válidos: BUYER, SELLER, MECHANIC")
    public ResponseEntity<AuthenticatedUserResource> signUp(@RequestBody SignUpResource resource) {
        var command = new SignUpCommand(resource.username(), resource.email(), resource.password(), resource.roles());
        var user = userCommandService.handle(command);
        return user.map(u -> new AuthenticatedUserResource(
                        u.getId(), u.getUsername(), null, u.getPrimaryRoleName()))
                .map(r -> new ResponseEntity<>(r, HttpStatus.CREATED))
                .orElse(ResponseEntity.badRequest().build());
    }

    /**
     * US-02: Login que retorna JWT válido para usar en Authorization: Bearer header
     */
    @PostMapping("/sign-in")
    @Operation(summary = "Iniciar sesión y obtener JWT")
    public ResponseEntity<AuthenticatedUserResource> signIn(@RequestBody SignInResource resource) {
        var command = new SignInCommand(resource.resolveIdentifier(), resource.password());
        var result = userCommandService.handle(command);
        return result.map(pair -> {
            var user = pair.getLeft();
            var token = pair.getRight();
            return ResponseEntity.ok(new AuthenticatedUserResource(
                    user.getId(), user.getUsername(), token, user.getPrimaryRoleName()));
        }).orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }
}
