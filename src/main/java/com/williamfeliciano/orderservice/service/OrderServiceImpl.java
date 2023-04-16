package com.williamfeliciano.orderservice.service;

import com.williamfeliciano.orderservice.entity.Order;
import com.williamfeliciano.orderservice.model.OrderRequest;
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
    @Override
    public Long placeOrder(OrderRequest orderRequest) {

        // Order Entity -> Save the order with orderStatus Created
        // Product Service -> Check inventory and reduce if available
        // Payment Service -> Try anc complete payment -> Success-> orderStatus COMPLETE else CANCELEED
        log.info("Placing Order Request: {}", orderRequest);
        // Creating Order Entity
        Order order = Order.builder()
                .amount(orderRequest.getAmount())
                .quantity(orderRequest.getQuantity())
                .orderStatus("CREATED")
                .orderDate(Instant.now())
                .productId(orderRequest.getProductId())
                .build();
        order = orderRepository.save(order);
        log.info("Order Placed Successfully with Order ID {}",order.getId());
        return order.getId();
    }
}
