package com.pe.platform.payment.interfaces.rest;

import com.pe.platform.shared.infrastructure.security.CurrentUser;
import com.pe.platform.payment.domain.model.aggregates.Transaction;
import com.pe.platform.payment.domain.model.commands.CreateTransactionCommand;
import com.pe.platform.payment.domain.model.queries.GetTransactionsByBuyerQuery;
import com.pe.platform.payment.domain.model.queries.GetTransactionsBySellerQuery;
import com.pe.platform.payment.infrastructure.persistence.jpa.TransactionRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Transaction REST controller — Payment BC
 * US-16: Comprador realiza pago
 * US-17: Vendedor recibe confirmación
 * US-18: Historial de transacciones
 * US-19: Reembolso
 */
@RestController
@RequestMapping("/api/v1/transactions")
@Tag(name = "Transactions", description = "Gestión de pagos y transacciones")
public class TransactionController {

    private final TransactionRepository transactionRepository;

    public TransactionController(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    /** US-16: Comprador inicia transacción de compra */
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_BUYER')")
    @Operation(summary = "Iniciar transacción de compra")
    public ResponseEntity<Map<String, Object>> createTransaction(
            @RequestBody Map<String, Object> body,
            @AuthenticationPrincipal CurrentUser currentUser) {

        var transaction = new Transaction(
                currentUser.getId(),
                Long.valueOf(body.get("sellerProfileId").toString()),
                Long.valueOf(body.get("vehicleId").toString()),
                Double.valueOf(body.get("amount").toString()),
                body.getOrDefault("paymentMethod", "CARD").toString()
        );
        transaction.complete(); //Simula pago exitoso (integrar Stripe/PayPal )
        var saved = transactionRepository.save(transaction);

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "id", saved.getId(),
                "status", saved.getStatus().name(),
                "amount", saved.getAmount(),
                "createdAt", saved.getCreatedAt().toString()
        ));
    }

    /** US-18: Historial del comprador autenticado */
    @GetMapping("/my/purchases")
    @PreAuthorize("hasAuthority('ROLE_BUYER')")
    @Operation(summary = "Mis compras")
    public ResponseEntity<List<Transaction>> getMyPurchases(
            @AuthenticationPrincipal CurrentUser currentUser) {
        return ResponseEntity.ok(
                transactionRepository.findByBuyerProfileId(currentUser.getId()));
    }

    /** US-17/18: Historial del vendedor autenticado */
    @GetMapping("/my/sales")
    @PreAuthorize("hasAuthority('ROLE_SELLER')")
    @Operation(summary = "Mis ventas")
    public ResponseEntity<List<Transaction>> getMySales(
            @AuthenticationPrincipal CurrentUser currentUser) {
        return ResponseEntity.ok(
                transactionRepository.findBySellerProfileId(currentUser.getId()));
    }

    /** US-19: Solicitar reembolso */
    @PutMapping("/{id}/refund")
    @PreAuthorize("hasAuthority('ROLE_BUYER')")
    @Operation(summary = "Solicitar reembolso")
    public ResponseEntity<Map<String, String>> refundTransaction(
            @PathVariable Long id,
            @AuthenticationPrincipal CurrentUser currentUser) {

        return transactionRepository.findById(id)
                .filter(t -> t.getBuyerProfileId().equals(currentUser.getId()))
                .map(t -> {
                    t.refund();
                    transactionRepository.save(t);
                    return ResponseEntity.ok(Map.of("status", "REFUNDED"));
                })
                .orElse(ResponseEntity.status(HttpStatus.FORBIDDEN).build());
    }
}
