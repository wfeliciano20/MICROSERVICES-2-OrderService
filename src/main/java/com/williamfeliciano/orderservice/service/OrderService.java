package com.williamfeliciano.orderservice.service;

import com.williamfeliciano.orderservice.model.OrderRequest;
import com.williamfeliciano.orderservice.model.OrderResponse;

public interface OrderService {
    Long placeOrder(OrderRequest orderRequest);

    OrderResponse getOrderDetails(long orderId);
}
