package com.pe.platform.payment.interfaces.rest.resources;

/** Datos para crear un PaymentIntent de Stripe: monto y moneda. */
public record CreatePaymentIntentResource(Double amount, String currency) {}
