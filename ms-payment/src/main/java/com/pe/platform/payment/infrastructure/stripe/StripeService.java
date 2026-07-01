package com.pe.platform.payment.infrastructure.stripe;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Servicio de integración con Stripe (pasarela de pago).
 * Crea PaymentIntents en modo test. La clave secreta se lee de una variable
 * de entorno (STRIPE_SECRET_KEY) por seguridad, nunca se escribe en el código.
 */
@Service
public class StripeService {

    @Value("${stripe.secret-key:}")
    private String secretKey;

    @PostConstruct
    public void init() {
        Stripe.apiKey = secretKey;
    }

    /**
     * Crea un PaymentIntent por el monto indicado (en la moneda dada) y devuelve
     * el clientSecret que el frontend usa para confirmar el pago.
     * Stripe trabaja en la unidad mínima (céntimos), por eso se multiplica por 100.
     */
    public Map<String, String> createPaymentIntent(double amount, String currency) throws StripeException {
        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount((long) (amount * 100))   // soles -> céntimos
                .setCurrency(currency == null ? "pen" : currency.toLowerCase())
                .setAutomaticPaymentMethods(
                        PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                .setEnabled(true)
                                .build())
                .build();

        PaymentIntent intent = PaymentIntent.create(params);
        return Map.of(
                "clientSecret", intent.getClientSecret(),
                "paymentIntentId", intent.getId()
        );
    }
}
