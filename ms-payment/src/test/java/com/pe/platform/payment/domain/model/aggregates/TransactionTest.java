package com.pe.platform.payment.domain.model.aggregates;

import com.pe.platform.payment.domain.model.valueobjects.PaymentStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios de la entidad Transaction (Payment BC)
 * Prueba la maquina de estados: PENDING -> COMPLETED -> REFUNDED
 *  logica de dominio pura
 */
class TransactionTest {

    private Transaction nuevaTransaccion() {
        return new Transaction(8L, 6L, 3L, 25000.0, "CARD");
    }

    @Test
    @DisplayName("Una transaccion nueva nace en estado PENDING")
    void nuevaTransaccion_estadoPending() {
        var tx = nuevaTransaccion();
        assertEquals(PaymentStatus.PENDING, tx.getStatus());
        assertEquals(25000.0, tx.getAmount());
    }

    @Test
    @DisplayName("complete() pasa la transaccion a COMPLETED")
    void completar_estadoCompleted() {
        var tx = nuevaTransaccion();
        tx.complete();
        assertEquals(PaymentStatus.COMPLETED, tx.getStatus());
    }

    @Test
    @DisplayName("No se puede completar dos veces (debe estar PENDING)")
    void completar_dosVeces_lanzaExcepcion() {
        var tx = nuevaTransaccion();
        tx.complete();
        assertThrows(IllegalStateException.class, tx::complete);
    }

    @Test
    @DisplayName("refund() solo funciona sobre una transaccion COMPLETED")
    void reembolsar_completada_ok() {
        var tx = nuevaTransaccion();
        tx.complete();
        tx.refund();
        assertEquals(PaymentStatus.REFUNDED, tx.getStatus());
    }

    @Test
    @DisplayName("No se puede reembolsar una transaccion que no esta COMPLETED")
    void reembolsar_pendiente_lanzaExcepcion() {
        var tx = nuevaTransaccion();
        assertThrows(IllegalStateException.class, tx::refund);
    }

    @Test
    @DisplayName("fail() marca la transaccion como FAILED con motivo")
    void fallar_estadoFailed() {
        var tx = nuevaTransaccion();
        tx.fail("Tarjeta rechazada");
        assertEquals(PaymentStatus.FAILED, tx.getStatus());
    }
}
