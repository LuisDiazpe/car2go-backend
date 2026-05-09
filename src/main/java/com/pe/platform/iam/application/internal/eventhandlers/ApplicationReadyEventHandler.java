package com.pe.platform.iam.application.internal.eventhandlers;

import com.pe.platform.iam.domain.model.commands.SeedRolesCommand;
import com.pe.platform.iam.domain.services.UserCommandService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
public class ApplicationReadyEventHandler {

    private static final Logger log = LoggerFactory.getLogger(ApplicationReadyEventHandler.class);
    private final UserCommandService userCommandService;

    public ApplicationReadyEventHandler(UserCommandService userCommandService) {
        this.userCommandService = userCommandService;
    }

    @EventListener
    public void on(ApplicationReadyEvent event) {
        log.info("Car2Go Platform started. Seeding roles...");
        userCommandService.handle(new SeedRolesCommand());
        log.info("Roles seeded: ROLE_BUYER, ROLE_SELLER, ROLE_MECHANIC, ROLE_ADMIN");
    }
}
