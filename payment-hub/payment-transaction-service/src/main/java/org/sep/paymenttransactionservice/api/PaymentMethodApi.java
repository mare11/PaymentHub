package org.sep.paymenttransactionservice.api;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.net.URI;

@FeignClient("payment-method")
public interface PaymentMethodApi {

    @PostMapping(value = "/create", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    PaymentResponse createPayment(URI baseUrl, @RequestBody PaymentRequest paymentRequest);

    @PostMapping(value = "/execute", consumes = MediaType.APPLICATION_JSON_VALUE)
    boolean executePayment(URI baseUrl, @RequestBody PaymentResponse paymentResponse);
}
