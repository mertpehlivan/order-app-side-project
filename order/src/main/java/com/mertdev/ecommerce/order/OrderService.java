package com.mertdev.ecommerce.order;

import com.mertdev.ecommerce.customer.CustomerClient;
import com.mertdev.ecommerce.exception.BusinessException;
import com.mertdev.ecommerce.kafka.OrderConfirmation;
import com.mertdev.ecommerce.kafka.OrderProducer;
import com.mertdev.ecommerce.orderline.OrderLineRequest;
import com.mertdev.ecommerce.orderline.OrderLineService;
import com.mertdev.ecommerce.payment.PaymentClient;
import com.mertdev.ecommerce.payment.PaymentRequest;
import com.mertdev.ecommerce.product.ProductClient;
import com.mertdev.ecommerce.product.PurchaseRequest;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository repository;
    private final CustomerClient customerClient;
    private final ProductClient productClient;
    private final OrderMapper mapper;
    private final OrderLineService orderLineService;
    private final OrderProducer orderProducer;
    private final PaymentClient paymentClient;
    public Integer createOrder(OrderRequest request) {
        //check the customer --> OpenFeign------------------------------------
        var customer = this.customerClient.findCustomerById(request.customerId())
                .orElseThrow(()->new BusinessException("Cannot create order :: No Customer exists with the provided ID"));
        //purchase the products --> product-ms (RestTemplate)------------------
        var purchaseProducts = this.productClient.purchaseRequests(request.products());
        //persist order-------------------------------
        var order = this.repository.save(mapper.toOrder(request));
        //persist order lines---------------------
        for (PurchaseRequest purchaseRequest: request.products()){
            orderLineService.saveOrderLine(
                    new OrderLineRequest(
                            null,
                            order.getId(),
                            purchaseRequest.productId(),
                            purchaseRequest.quantity()
                    )
            );
        }
        var paymentRequest = new PaymentRequest(
                request.amount(),
                request.paymentMethod(),
                order.getId(),
                order.getReference(),
                customer

        );
        paymentClient.requestOrderPayment(paymentRequest);
        // todo start payment process
        orderProducer.sendOrderConfirmation(
                new OrderConfirmation(
                        request.reference(),
                        request.amount(),
                        request.paymentMethod(),
                        customer,
                        purchaseProducts
                )
        );
        //send the order confirmation --> notification-ms (kafka)
        return order.getId();
    }

    public List<OrderResponse> findAll() {
        return repository.findAll().stream().map(mapper::formOrder).collect(Collectors.toList());
    }

    public OrderResponse findById(Integer orderId) {
        return repository.findById(orderId)
                .map(mapper::formOrder)
                .orElseThrow(()-> new EntityNotFoundException(String.format("No order found with the provided ID : %d",orderId)));
    }
}
