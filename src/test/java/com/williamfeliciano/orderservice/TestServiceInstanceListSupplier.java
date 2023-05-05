package com.williamfeliciano.orderservice;

import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;

public class TestServiceInstanceListSupplier implements ServiceInstanceListSupplier {
    @Override
    public String getServiceId() {
        return null;
    }

    @Override
    public Flux<List<ServiceInstance>> get() {
        List<ServiceInstance> result = new ArrayList<>();
        result.add(new DefaultServiceInstance(
                "PAYMENT_SERVICE",
                "PAYMENT_SERVICE",
                "localhost",
                8081,
                false
        ));
        result.add(new DefaultServiceInstance(
                "PRODUCT_SERVICE",
                "PRODUCT_SERVICE",
                "localhost",
                8081,
                false
        ));
        return Flux.just(result);
    }
}
