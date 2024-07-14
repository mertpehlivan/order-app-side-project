package com.mertdev.ecommerce.payment;

import com.mertdev.ecommerce.customer.CustomerResponse;
import com.mertdev.ecommerce.order.PaymentMethod;

import java.math.BigDecimal;

public record PaymentRequest(
        BigDecimal amount,
        PaymentMethod paymentMethod,
        Integer orderId,
        String orderReference,
        CustomerResponse customer
) {
}
