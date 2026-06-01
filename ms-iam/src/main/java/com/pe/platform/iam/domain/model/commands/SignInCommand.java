package com.pe.platform.iam.domain.model.commands;

/**
 * US-02: Como usuario quiero iniciar sesión y recibir un token JWT.
 */
public record SignInCommand(String username, String password) {}
