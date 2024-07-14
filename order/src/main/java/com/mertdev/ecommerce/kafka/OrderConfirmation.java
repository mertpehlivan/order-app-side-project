package com.mertdev.ecommerce.kafka;

import com.mertdev.ecommerce.customer.CustomerResponse;
import com.mertdev.ecommerce.order.PaymentMethod;
import com.mertdev.ecommerce.product.PurchaseResponse;

import java.math.BigDecimal;
import java.util.List;

public record OrderConfirmation(
        String orderReference,
        BigDecimal totalAmount,
        PaymentMethod paymentMethod,
        CustomerResponse customerResponse,
        List<PurchaseResponse> products
) {
}
