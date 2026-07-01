package com.pe.platform.inspection.domain.model.commands;

public record CompleteInspectionCommand(Long inspectionId, Long mechanicProfileId,
                                        String notes, String certificateDetails,
                                        Double inspectionFee) {}
