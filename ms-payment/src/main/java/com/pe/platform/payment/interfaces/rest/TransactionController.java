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
 * US-16: Comprador realiza pago (ahora con Stripe)
 * US-17: Vendedor recibe confirmación
 * US-18: Historial de transacciones
 * US-19: Reembolso
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

    /**
     * Paso 1 del pago con Stripe: crea un PaymentIntent y devuelve el clientSecret.
     * El frontend usa ese clientSecret para mostrar el formulario de tarjeta y confirmar.
     */
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
     * Paso 2 (US-16): después de que Stripe confirma el pago en el frontend,
     * se registra la transacción y se marca el vehículo como SOLD.
     */
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_BUYER')")
    @Operation(summary = "Registrar transacción de compra (tras confirmar el pago)")
    public ResponseEntity<Map<String, Object>> createTransaction(
            @RequestBody Map<String, Object> body,
            @AuthenticationPrincipal CurrentUser currentUser) {

        Long vehicleId = Long.valueOf(body.get("vehicleId").toString());

        var transaction = new Transaction(
                currentUser.getId(),
                Long.valueOf(body.get("sellerProfileId").toString()),
                vehicleId,
                Double.valueOf(body.get("amount").toString()),
                body.getOrDefault("paymentMethod", "STRIPE").toString()
        );
        transaction.complete();
        var saved = transactionRepository.save(transaction);

        // Comunicación entre microservicios: avisar a ms-vehicle que el auto se vendió.
        try {
            vehicleClient.markSold(vehicleId);
        } catch (Exception e) {
            System.err.println("No se pudo marcar el vehiculo " + vehicleId + " como SOLD: " + e.getMessage());
        }

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
