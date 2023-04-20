package com.williamfeliciano.orderservice.service;

import com.williamfeliciano.orderservice.entity.Order;
import com.williamfeliciano.orderservice.external.client.PaymentService;
import com.williamfeliciano.orderservice.external.client.ProductService;
import com.williamfeliciano.orderservice.external.exception.CustomException;
import com.williamfeliciano.orderservice.external.request.PaymentRequest;
import com.williamfeliciano.orderservice.external.response.ProductResponse;
import com.williamfeliciano.orderservice.model.OrderRequest;
import com.williamfeliciano.orderservice.model.OrderResponse;
import com.williamfeliciano.orderservice.repository.OrderRepository;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@Log4j2
@AllArgsConstructor
public class OrderServiceImpl implements OrderService{
    private OrderRepository orderRepository;
    private ProductService productService;
    private PaymentService paymentService;
    @Override
    public Long placeOrder(OrderRequest orderRequest) {

        // Order Entity -> Save the order with orderStatus Created
        // Product Service -> Check inventory and reduce if available
        // Payment Service -> Try anc complete payment -> Success-> orderStatus COMPLETE else CANCELEED
        log.info("Placing Order Request: {}", orderRequest);

        productService.reduceQuantity(orderRequest.getProductId(),orderRequest.getQuantity());

        log.info("Creating Order with status CREATED");
        // Creating Order Entity
        Order order = Order.builder()
                .amount(orderRequest.getAmount())
                .quantity(orderRequest.getQuantity())
                .orderStatus("CREATED")
                .orderDate(Instant.now())
                .productId(orderRequest.getProductId())
                .build();
        order = orderRepository.save(order);
        log.info("Calling Payment Service To Complete Payment");

        PaymentRequest paymentRequest =
                PaymentRequest.builder()
                        .orderId(order.getId())
                        .paymentMode(orderRequest.getPaymentMode())
                        .amount(orderRequest.getAmount())
                .build();

        String orderStatus = null;
        try {
            paymentService.doPayment(paymentRequest);
            log.info("Payment done successfully changing status to placed");
            orderStatus = "PLACED";
        }catch (Exception e) {
            log.error("Error occurred in payment. Changing order Status to PAYMENT_FAILED");
            orderStatus = "PAYMENT_FAILED";
        }

        order.setOrderStatus(orderStatus);
        orderRepository.save(order);

        log.info("Order Placed Successfully with Order ID {}",order.getId());
        return order.getId();
    }

    @Override
    public OrderResponse getOrderDetails(long orderId) {
        log.info("Getting Order Details for order with id {}", orderId);
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new CustomException("Order not found with order id: " + orderId, "NOT_FOUND", 404));
        log.info("Calling Product Service to get Product  with Id {}", order.getProductId());
        ProductResponse productResponse = productService.getProductById(order.getProductId()).getBody();
        return OrderResponse.builder()
                .orderId(order.getId())
                .orderStatus(order.getOrderStatus())
                .orderDate(order.getOrderDate())
                .amount(order.getAmount())
                .productDetails(OrderResponse.ProductDetails.builder()
                        .productId(productResponse != null ? productResponse.getProductId() : 0)
                        .productName(productResponse != null ? productResponse.getProductName() : null)
                        .price(productResponse != null ? productResponse.getPrice() : 0)
                        .quantity(productResponse != null ? productResponse.getQuantity() : 0)
                        .build()).build();
    }
}
