package com.pe.platform.iam.domain.model.commands;

import java.util.List;
/**
 * US-01: Como usuario quiero registrarme con un rol (BUYER, SELLER, MECHANIC).
 */
public record SignUpCommand(String username, String email, String password, List<String> roles) {}





