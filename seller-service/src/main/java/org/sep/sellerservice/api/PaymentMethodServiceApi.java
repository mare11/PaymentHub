package org.sep.sellerservice.api;

import org.sep.sellerservice.model.PaymentMethod;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Optional;

@FeignClient("payment-method-service")
public interface PaymentMethodServiceApi {

    @GetMapping(value = "/method", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<List<PaymentMethod>> getAllPaymentMethods();

    @PostMapping(value = "/method", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<PaymentMethod> addPaymentMethod(@RequestBody PaymentMethod paymentMethod);
}