package org.sep.paymentgatewayservice.methodapi;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.net.URI;

@FeignClient("payment-method")
public interface PaymentMethodApi {

    @PostMapping(value = "/create", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<PaymentMethodResponse> createPayment(URI baseUrl, @RequestBody PaymentMethodRequest paymentMethodRequest);

    @PostMapping(value = "/complete", consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Void> completePayment(URI baseUrl, @RequestBody PaymentCompleteRequest paymentCompleteRequest);
}
