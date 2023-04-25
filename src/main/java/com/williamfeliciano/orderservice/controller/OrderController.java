package com.williamfeliciano.orderservice.controller;

import com.williamfeliciano.orderservice.model.OrderRequest;
import com.williamfeliciano.orderservice.model.OrderResponse;
import com.williamfeliciano.orderservice.service.OrderService;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/order")
@Log4j2
@AllArgsConstructor
public class OrderController {

    private OrderService orderService;
    @PreAuthorize("hasAuthority('Customer')")
    @PostMapping("/placeOrder")
    public ResponseEntity<Long> placeOrder(@RequestBody OrderRequest orderRequest){
        Long orderId = orderService.placeOrder(orderRequest);
        log.info("OrderID: {}", orderId);
        return new ResponseEntity<>(orderId, HttpStatus.CREATED);
    }

    @PreAuthorize("hasAuthority('Customer') || hasAuthority('ADMIN')")
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrderDetails(@PathVariable("orderId")long orderId){
        OrderResponse orderResponse = orderService.getOrderDetails(orderId);
        return ResponseEntity.ok(orderResponse);
    }
}
