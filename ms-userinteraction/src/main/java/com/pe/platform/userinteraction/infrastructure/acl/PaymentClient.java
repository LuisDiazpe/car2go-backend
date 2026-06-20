package com.pe.platform.userinteraction.infrastructure.acl;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

/**
 * Anti-Corruption Layer (ACL) hacia ms-payment.
 *
 * Se usa para validar la regla de negocio: un usuario solo puede dejar RESEÑAS
 * (estrellas) si tiene al menos 1 transacción. Esa información la tiene ms-payment,
 * dueño de las transacciones, así que se le consulta vía REST con OpenFeign.
 *
 * Eureka resuelve "ms-payment" a la dirección real del servicio (lb://).
 */
@FeignClient(name = "ms-payment")
public interface PaymentClient {

    /** Cuenta las transacciones de un usuario (como comprador o vendedor). */
    @GetMapping("/api/v1/transactions/count/{profileId}")
    Map<String, Object> countTransactions(@PathVariable("profileId") Long profileId);
}
