package com.williamfeliciano.orderservice.service;


import com.williamfeliciano.orderservice.entity.Order;
import com.williamfeliciano.orderservice.external.client.PaymentService;
import com.williamfeliciano.orderservice.external.client.ProductService;
import com.williamfeliciano.orderservice.external.exception.CustomException;
import com.williamfeliciano.orderservice.external.request.PaymentRequest;
import com.williamfeliciano.orderservice.external.response.PaymentResponse;
import com.williamfeliciano.orderservice.external.response.ProductResponse;
import com.williamfeliciano.orderservice.model.OrderRequest;
import com.williamfeliciano.orderservice.model.OrderResponse;
import com.williamfeliciano.orderservice.model.PaymentMode;
import com.williamfeliciano.orderservice.repository.OrderRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@SpringBootTest
public class OrderServiceImplTest {
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private ProductService productService;
    @Mock
    private PaymentService paymentService;

    @InjectMocks
    OrderService orderService = new OrderServiceImpl();

    @DisplayName("Get Order - Success Scenario")
    @Test
    void test_When_Order_Success(){
        Order order = getMockOrder();
//        ProductResponse productResponse = getMockProductResponse().getBody();
        // Mocking
        when(orderRepository.findById(anyLong()))
                .thenReturn(Optional.of(order));
        when(productService.getProductById(anyLong()))
                .thenReturn(getMockProductResponse());
        when(paymentService.getPaymentDetailsByOrderId(anyLong()))
                .thenReturn(getMockPaymentResponse());
        // Actual
        OrderResponse orderResponse = orderService.getOrderDetails(1);

        // Verification
        verify(orderRepository,times(1)).findById(anyLong());
        verify(productService,times(1)).getProductById(anyLong());
        verify(paymentService,times(1)).getPaymentDetailsByOrderId(anyLong());
        // Assert
        assertNotNull(orderResponse);
        assertEquals(order.getId(),orderResponse.getOrderId());
    }

    @DisplayName("Get Orders - Failure Scenario")
    @Test
    void test_When_Order_Not_Found_then_NOT_FOUND(){
        when(orderRepository.findById(anyLong()))
                .thenReturn(Optional.ofNullable(null));

        CustomException exception = assertThrows(CustomException.class,()->orderService.getOrderDetails(1));

        verify(orderRepository,times(1)).findById(anyLong());
        assertEquals("NOT_FOUND",exception.getErrorCode());
        assertEquals(404,exception.getStatus());
    }


    @DisplayName("Place Order - Success Scenario")
    @Test
    void test_When_Place_Order_Success(){
        Order order = getMockOrder();
        OrderRequest orderRequest = getMockOrderRequest();


        when(orderRepository.save(any(Order.class)))
                .thenReturn(order);
        when(productService.reduceQuantity(anyLong(),anyLong()))
                .thenReturn(new ResponseEntity<Void>(HttpStatus.OK));
        when(paymentService.doPayment(any(PaymentRequest.class)))
                .thenReturn(new ResponseEntity<Long>(1L,HttpStatus.OK));

        long orderId = orderService.placeOrder(orderRequest);

        verify(orderRepository,times(2))
                .save(any(Order.class));
        verify(productService,times(1))
                .reduceQuantity(anyLong(),anyLong());
        verify(paymentService,times(1))
                .doPayment(any(PaymentRequest.class));

        assertEquals(order.getId(),orderId);
    }


    @DisplayName("Place Order - Failure Scenario")
    @Test
    void test_When_Place_Order_Fails_Then_Order_Placed(){
        Order order = getMockOrder();
        OrderRequest orderRequest = getMockOrderRequest();


        when(orderRepository.save(any(Order.class)))
                .thenReturn(order);
        when(productService.reduceQuantity(anyLong(),anyLong()))
                .thenReturn(new ResponseEntity<Void>(HttpStatus.OK));
        when(paymentService.doPayment(any(PaymentRequest.class)))
                .thenThrow(new RuntimeException());

        long orderId = orderService.placeOrder(orderRequest);

        verify(orderRepository,times(2))
                .save(any(Order.class));
        verify(productService,times(1))
                .reduceQuantity(anyLong(),anyLong());
        verify(paymentService,times(1))
                .doPayment(any(PaymentRequest.class));

        assertEquals(order.getId(), orderId);
        assertEquals(order.getOrderStatus(), "PAYMENT_FAILED");
    }
    private OrderRequest getMockOrderRequest() {
        return OrderRequest.builder()
                .productId(1)
                .amount(100)
                .paymentMode(PaymentMode.DEBIT_CARD)
                .quantity(10)
                .build();
    }

    private ResponseEntity<PaymentResponse> getMockPaymentResponse() {
        PaymentResponse paymentResponse = PaymentResponse.builder()
                .paymentId(1)
                .paymentDate(Instant.now())
                .paymentMode(PaymentMode.DEBIT_CARD)
                .amount(200)
                .orderId(1)
                .paymentStatus("ACEPTED")
                .build();
        return new ResponseEntity<>(paymentResponse,HttpStatus.OK);
    }

    private ResponseEntity<ProductResponse> getMockProductResponse() {
        ProductResponse productResponse = ProductResponse.builder()
                .productId(2)
                .productName("iPhone")
                .price(100)
                .quantity(200)
                .build();
        return new ResponseEntity<>(productResponse, HttpStatus.OK);
    }

    private Order getMockOrder() {
        return Order.builder()
                .orderStatus("PLACED")
                .orderDate(Instant.now())
                .id(1)
                .amount(100)
                .quantity(200)
                .productId(2)
                .build();
    }
}