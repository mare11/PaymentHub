package org.sep.paymentgatewayservice.seller.api;

import org.sep.paymentgatewayservice.payment.entity.FeignConfiguration;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(value = "seller-service/api/payment_method", configuration = FeignConfiguration.class)
public interface PaymentMethodServiceApi {

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<List<PaymentMethod>> getAllPaymentMethods();

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Void> addPaymentMethod(@RequestBody PaymentMethod paymentMethod);
}