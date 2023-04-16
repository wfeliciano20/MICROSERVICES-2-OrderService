package com.williamfeliciano.orderservice.service;

import com.williamfeliciano.orderservice.model.OrderRequest;

public interface OrderService {
    Long placeOrder(OrderRequest orderRequest);
}
