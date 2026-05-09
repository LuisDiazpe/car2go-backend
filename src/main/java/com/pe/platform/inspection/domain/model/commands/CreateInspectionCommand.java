package com.pe.platform.inspection.domain.model.commands;

import java.time.LocalDateTime;

public record CreateInspectionCommand(Long vehicleId, Long sellerProfileId, LocalDateTime scheduledAt) {}
