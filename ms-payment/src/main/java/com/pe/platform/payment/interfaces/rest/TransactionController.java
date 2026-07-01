package com.pe.platform.payment.interfaces.rest;

import com.pe.platform.shared.infrastructure.security.CurrentUser;
import com.pe.platform.payment.domain.model.aggregates.Transaction;
import com.pe.platform.payment.infrastructure.acl.VehicleClient;
import com.pe.platform.payment.infrastructure.persistence.jpa.TransactionRepository;
import com.pe.platform.payment.infrastructure.stripe.StripeService;
import com.pe.platform.payment.interfaces.rest.resources.CreatePaymentIntentResource;
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
 * Pago con tarjeta (Stripe) -> COMPLETED + auto SOLD inmediato.
 * Pago en efectivo -> PENDING, el vendedor confirma luego (auto pasa a SOLD al confirmar).
 */
@RestController
@RequestMapping("/api/v1/transactions")
@Tag(name = "Transactions", description = "Gestión de pagos y transacciones")
public class TransactionController {

    private final TransactionRepository transactionRepository;
    private final VehicleClient vehicleClient;
    private final StripeService stripeService;

    public TransactionController(TransactionRepository transactionRepository,
                                 VehicleClient vehicleClient,
                                 StripeService stripeService) {
        this.transactionRepository = transactionRepository;
        this.vehicleClient = vehicleClient;
        this.stripeService = stripeService;
    }

    /** Stripe paso 1: crear PaymentIntent (solo para pago con tarjeta) */
    @PostMapping("/create-payment-intent")
    @PreAuthorize("hasAuthority('ROLE_BUYER')")
    @Operation(summary = "Crear intención de pago en Stripe (devuelve clientSecret)")
    public ResponseEntity<Map<String, String>> createPaymentIntent(
            @RequestBody CreatePaymentIntentResource resource) {
        try {
            var result = stripeService.createPaymentIntent(
                    resource.amount(),
                    resource.currency() == null ? "pen" : resource.currency());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "No se pudo crear el pago: " + e.getMessage()));
        }
    }

    /**
     * Registrar transacción.
     * - paymentMethod = STRIPE: pago ya confirmado -> COMPLETED + auto SOLD.
     * - paymentMethod = CASH:   queda PENDING, el auto NO se marca SOLD todavía.
     */
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_BUYER')")
    @Operation(summary = "Registrar transacción de compra (tarjeta o efectivo)")
    public ResponseEntity<Map<String, Object>> createTransaction(
            @RequestBody Map<String, Object> body,
            @AuthenticationPrincipal CurrentUser currentUser) {

        Long vehicleId = Long.valueOf(body.get("vehicleId").toString());
        String method = body.getOrDefault("paymentMethod", "STRIPE").toString().toUpperCase();

        var transaction = new Transaction(
                currentUser.getId(),
                Long.valueOf(body.get("sellerProfileId").toString()),
                vehicleId,
                Double.valueOf(body.get("amount").toString()),
                method
        );

        boolean isCash = method.equals("CASH") || method.equals("EFECTIVO");

        if (!isCash) {
            // Pago con tarjeta: ya se confirmó en el frontend con Stripe
            transaction.complete();
        }
        // Si es efectivo: se queda en PENDING (no se llama a complete())

        var saved = transactionRepository.save(transaction);

        // Solo marcar SOLD si el pago fue con tarjeta (completado).
        // Si es efectivo, el auto NO se marca SOLD hasta que el vendedor confirme.
        if (!isCash) {
            try {
                vehicleClient.markSold(vehicleId);
            } catch (Exception e) {
                System.err.println("No se pudo marcar el vehiculo " + vehicleId + " como SOLD: " + e.getMessage());
            }
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "id", saved.getId(),
                "status", saved.getStatus().name(),
                "amount", saved.getAmount(),
                "paymentMethod", method,
                "createdAt", saved.getCreatedAt().toString()
        ));
    }

    /**
     * El VENDEDOR confirma que recibió el pago en efectivo.
     * La transacción pasa a COMPLETED y el auto se marca como SOLD.
     */
    @PutMapping("/{id}/confirm-cash")
    @PreAuthorize("hasAuthority('ROLE_SELLER')")
    @Operation(summary = "Vendedor confirma el pago en efectivo (marca el auto como vendido)")
    public ResponseEntity<Map<String, Object>> confirmCashPayment(
            @PathVariable Long id,
            @AuthenticationPrincipal CurrentUser currentUser) {

        var txOpt = transactionRepository.findById(id);
        if (txOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        var tx = txOpt.get();

        // Solo el vendedor dueño de la venta puede confirmar
        if (!tx.getSellerProfileId().equals(currentUser.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "No eres el vendedor de esta transacción"));
        }

        try {
            tx.complete(); // PENDING -> COMPLETED
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "La transacción ya fue procesada"));
        }
        transactionRepository.save(tx);

        // Ahora sí, marcar el auto como vendido
        try {
            vehicleClient.markSold(tx.getVehicleId());
        } catch (Exception e) {
            System.err.println("No se pudo marcar el vehiculo " + tx.getVehicleId() + " como SOLD: " + e.getMessage());
        }

        return ResponseEntity.ok(Map.of(
                "id", tx.getId(),
                "status", tx.getStatus().name(),
                "vehicleId", tx.getVehicleId()
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

    /** US-17/18: Historial del vendedor autenticado (incluye las ventas en efectivo pendientes) */
    @GetMapping("/my/sales")
    @PreAuthorize("hasAuthority('ROLE_SELLER')")
    @Operation(summary = "Mis ventas")
    public ResponseEntity<List<Transaction>> getMySales(
            @AuthenticationPrincipal CurrentUser currentUser) {
        return ResponseEntity.ok(
                transactionRepository.findBySellerProfileId(currentUser.getId()));
    }

    /** Endpoint interno: cuenta transacciones de un usuario (para validar reseñas) */
    @GetMapping("/count/{profileId}")
    @Operation(summary = "Contar transacciones de un usuario (uso interno entre microservicios)")
    public ResponseEntity<Map<String, Object>> countTransactions(@PathVariable Long profileId) {
        long asBuyer = transactionRepository.findByBuyerProfileId(profileId).size();
        long asSeller = transactionRepository.findBySellerProfileId(profileId).size();
        long total = asBuyer + asSeller;
        return ResponseEntity.ok(Map.of(
                "profileId", profileId,
                "total", total,
                "hasTransactions", total > 0
        ));
    }

    /**
     * Cuenta cuántos compradores están interesados en un vehículo (transacciones
     * en efectivo PENDING). Se usa para mostrar "X personas quieren este auto".
     * Endpoint interno consumido por ms-vehicle / mostrado de forma anónima.
     */
    @GetMapping("/interested/{vehicleId}")
    @Operation(summary = "Contar interesados (compras en efectivo pendientes) de un vehículo")
    public ResponseEntity<Map<String, Object>> countInterested(@PathVariable Long vehicleId) {
        long interested = transactionRepository.findByVehicleId(vehicleId).stream()
                .filter(t -> t.getStatus().name().equals("PENDING"))
                .count();
        return ResponseEntity.ok(Map.of(
                "vehicleId", vehicleId,
                "interested", interested
        ));
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
