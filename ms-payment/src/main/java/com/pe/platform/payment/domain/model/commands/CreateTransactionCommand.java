package com.pe.platform.payment.domain.model.commands;

public record CreateTransactionCommand(Long buyerProfileId, Long sellerProfileId,
                                       Long vehicleId, Double amount, String paymentMethod) {}
